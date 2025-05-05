package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.mappers.genre.GenreRowMapper;
import ru.yandex.practicum.filmorate.mappers.mpa.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class RealDbFilmStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;
    private final GenreRowMapper genreRowMapper;
    private final MpaRowMapper mpaRowMapper;


    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO PUBLIC.\"FILM\" (NAME, DESCRIPTION, DURATION, RELEASE_DATE, MPA_RATING_ID) VALUES(?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setInt(3, film.getDuration());
            ps.setObject(4, film.getReleaseDate());
            ps.setInt(5, film.getMpaRating().getId());
            return ps;
        }, keyHolder);

        // Получаем сгенерированный ID
        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        film.getGenres().forEach(genre -> {
            String sql3 = "INSERT INTO public.\"FILM_GENRES\" (FILM_ID, GENRE_ID) VALUES(?, ?)";
            jdbc.update(sql3, generatedId, genre.getId());
        });

        String sql4 = "SELECT g.Id, g.Name FROM public.\"GENRE\" g JOIN public.\"FILM_GENRES\" fg ON (g.Id = fg.GENRE_ID) " +
                "WHERE fg.FILM_ID = ?";
        List<Genre> genres = jdbc.query(sql4, genreRowMapper, generatedId);
        film.setGenres(genres);

        String sql5 = "SELECT r.Id, r.Name FROM public.\"MPA_RATING\" r " +
                "WHERE r.Id = ?";
        MpaRating mpaRating = jdbc.queryForObject(sql5, mpaRowMapper, film.getMpaRating().getId());
        film.setMpaRating(mpaRating);

        film.setId(generatedId);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getMpaRating() == null) {
            String sql = "UPDATE public.\"FILM\" SET name = ?, description = ?, duration = ?, release_date = ? WHERE id = ?";
            jdbc.update(sql, film.getName(), film.getDescription(), film.getDuration(), film.getReleaseDate(), film.getId());
        }
        else {
            String sql = "UPDATE public.\"FILM\" SET name = ?, description = ?, duration = ?, release_date = ?, mpa_rating_id = ? WHERE id = ?";
            jdbc.update(sql, film.getName(), film.getDescription(), film.getDuration(), film.getReleaseDate(), film.getMpaRating().getId(), film.getId());
        }

        String sql2 = "DELETE FROM public.\"FILM_GENRES\" WHERE FILM_ID = ?";
        jdbc.update(sql2, film.getId());

        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> {
                String sql3 = "INSERT INTO public.\"FILM_GENRES\" (FILM_ID, GENRE_ID) VALUES(?, ?)";
                jdbc.update(sql3, film.getId(), genre.getId());
            });
        }

        return getFilm(film.getId());
    }

    @Override
    public void delete(long id) {
        String query = "DELETE FROM public.\"FILM\" WHERE id = ?";
        jdbc.update(query, id);
    }

    @Override
    public Film getFilm(long id) {
        String query = "SELECT f.*, r.Name mpa_rating_name FROM public.\"FILM\" f LEFT JOIN public.\"MPA_RATING\" r ON (f.mpa_rating_id = r.Id) WHERE f.id = ?";
        List<Film> films = jdbc.query(query, mapper, id);
        if ((long) films.size() == 0) {
            throw new NotFoundException(String.format("Фильм с id=%d не найден", id));
        }
        String genreQuery = "SELECT g.Id, g.Name FROM public.\"GENRE\" g JOIN public.\"FILM_GENRES\" fg " +
                "ON (fg.Genre_id = g.Id) WHERE fg.film_id = ?";
        Film film = films.getFirst();
        List<Genre> genres = jdbc.query(genreQuery, genreRowMapper, film.getId());
        film.setGenres(genres);
        return film;
    }

    @Override
    public List<Film> getFilms() {
        String query = "SELECT f.*, r.Name mpa_rating_name FROM public.\"FILM\" f LEFT JOIN public.\"MPA_RATING\" r ON (f.mpa_rating_id =  r.Id)";
        String genreQuery = "SELECT g.Id, g.Name FROM public.\"GENRE\" g JOIN public.\"FILM_GENRES\" fg " +
                "ON (fg.Genre_id = g.Id) WHERE fg.film_id = ?";
        List<Film> films = jdbc.query(query, mapper);
        films.forEach(film -> {
            List<Genre> genres = jdbc.query(genreQuery, genreRowMapper, film.getId());
            film.setGenres(genres);
        });
        return films;
    }

    @Override
    public void ensureFilmExists(long id) throws NotFoundException {
        String query = "SELECT count(*) FROM public.\"FILM\" WHERE id = ?";
        long count = jdbc.queryForObject(query, Long.class, id);
        if (count == 0) {
            throw new NotFoundException(String.format("Фильм с id=%d не найден", id));
        }
    }
}
