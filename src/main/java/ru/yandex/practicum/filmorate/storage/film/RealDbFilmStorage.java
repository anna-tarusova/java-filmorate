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

    private final String INSERT_NEW_FILM = "INSERT INTO PUBLIC.\"FILM\" (NAME, DESCRIPTION, DURATION, RELEASE_DATE, MPA_RATING_ID) VALUES(?, ?, ?, ?, ?)";
    private final String INSERT_FILM_GENRES = "INSERT INTO public.\"FILM_GENRES\" (FILM_ID, GENRE_ID) VALUES(?, ?)";
    private final String SELECT_GENRES_OF_FILM = "SELECT g.Id, g.Name FROM public.\"GENRE\" g JOIN public.\"FILM_GENRES\" fg ON (g.Id = fg.GENRE_ID) " +
            "WHERE fg.FILM_ID = ?";
    private final String SELECT_MPA_RATING = "SELECT r.Id, r.Name FROM public.\"MPA_RATING\" r " +
            "WHERE r.Id = ?";
    private final String UPDATE_USER_WIHOUT_MPA = "UPDATE public.\"FILM\" SET name = ?, description = ?, duration = ?, release_date = ? WHERE id = ?";
    private final String UPDATE_USER = "UPDATE public.\"FILM\" SET name = ?, description = ?, duration = ?, release_date = ?, mpa_rating_id = ? WHERE id = ?";;
    private final String CLEAR_GENRES_OF_FILM = "DELETE FROM public.\"FILM_GENRES\" WHERE FILM_ID = ?";
    private final String ADD_GENRE_TO_FILM = "INSERT INTO public.\"FILM_GENRES\" (FILM_ID, GENRE_ID) VALUES(?, ?)";
    private final String DELETE_FILM = "DELETE FROM public.\"FILM\" WHERE id = ?";
    private final String SELECT_FILM = "SELECT f.*, r.Name mpa_rating_name FROM public.\"FILM\" f LEFT JOIN public.\"MPA_RATING\" r ON (f.mpa_rating_id = r.Id) WHERE f.id = ?";
    private final String SELECT_ALL_FILMS = "SELECT f.*, r.Name mpa_rating_name FROM public.\"FILM\" f LEFT JOIN public.\"MPA_RATING\" r ON (f.mpa_rating_id =  r.Id)";
    private final String CHECK_IF_FILM_EXISTS = "SELECT count(*) FROM public.\"FILM\" WHERE id = ?";


    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_NEW_FILM, Statement.RETURN_GENERATED_KEYS);
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
            jdbc.update(INSERT_FILM_GENRES, generatedId, genre.getId());
        });

        List<Genre> genres = jdbc.query(SELECT_GENRES_OF_FILM, genreRowMapper, generatedId);
        film.setGenres(genres);

        MpaRating mpaRating = jdbc.queryForObject(SELECT_MPA_RATING, mpaRowMapper, film.getMpaRating().getId());
        film.setMpaRating(mpaRating);

        film.setId(generatedId);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getMpaRating() == null) {
            jdbc.update(UPDATE_USER_WIHOUT_MPA, film.getName(), film.getDescription(), film.getDuration(), film.getReleaseDate(), film.getId());
        }
        else {
            jdbc.update(UPDATE_USER, film.getName(), film.getDescription(), film.getDuration(), film.getReleaseDate(), film.getMpaRating().getId(), film.getId());
        }

        jdbc.update(CLEAR_GENRES_OF_FILM, film.getId());

        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> {
                jdbc.update(ADD_GENRE_TO_FILM, film.getId(), genre.getId());
            });
        }

        return getFilm(film.getId());
    }

    @Override
    public void delete(long id) {
        jdbc.update(DELETE_FILM, id);
    }

    @Override
    public Film getFilm(long id) {
        List<Film> films = jdbc.query(SELECT_FILM, mapper, id);
        if ((long) films.size() == 0) {
            throw new NotFoundException(String.format("Фильм с id=%d не найден", id));
        }

        Film film = films.getFirst();
        List<Genre> genres = jdbc.query(SELECT_GENRES_OF_FILM, genreRowMapper, film.getId());
        film.setGenres(genres);
        return film;
    }

    @Override
    public List<Film> getFilms() {
        List<Film> films = jdbc.query(SELECT_ALL_FILMS, mapper);
        films.forEach(film -> {
            List<Genre> genres = jdbc.query(SELECT_GENRES_OF_FILM, genreRowMapper, film.getId());
            film.setGenres(genres);
        });
        return films;
    }

    @Override
    public void ensureFilmExists(long id) throws NotFoundException {
        long count = jdbc.queryForObject(CHECK_IF_FILM_EXISTS, Long.class, id);
        if (count == 0) {
            throw new NotFoundException(String.format("Фильм с id=%d не найден", id));
        }
    }
}
