package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.mappers.genre.GenreRowMapper;
import ru.yandex.practicum.filmorate.mappers.mpa.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Qualifier("RealDbFilmStorage")
public class RealDbFilmStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;
    private final GenreRowMapper genreRowMapper;
    private final MpaRowMapper mpaRowMapper;

    private static final String INSERT_NEW_FILM = "INSERT INTO PUBLIC.\"FILM\" (NAME, DESCRIPTION, DURATION, RELEASE_DATE, MPA_RATING_ID) VALUES(?, ?, ?, ?, ?)";
    private static final String INSERT_FILM_GENRES = "INSERT INTO public.\"FILM_GENRES\" (FILM_ID, GENRE_ID) VALUES(?, ?)";
    private static final String SELECT_GENRES_OF_FILM = "SELECT g.Id, g.Name FROM public.\"GENRE\" g JOIN public.\"FILM_GENRES\" fg ON (g.Id = fg.GENRE_ID) " +
            "WHERE fg.FILM_ID = ?";
    private static final String SELECT_USER_IDS_WHO_LIKED = "SELECT fl.User_Id FROM public.\"FILM_LIKES\" fl " +
            "WHERE fl.FILM_ID = ?";
    private static final String SELECT_MPA_RATING = "SELECT r.Id, r.Name FROM public.\"MPA_RATING\" r " +
            "WHERE r.Id = ?";
    private static final String UPDATE_FILM_WITHOUT_MPA = "UPDATE public.\"FILM\" SET name = ?, description = ?, duration = ?, release_date = ? WHERE id = ?";
    private static final String UPDATE_FILM = "UPDATE public.\"FILM\" SET name = ?, description = ?, duration = ?, release_date = ?, mpa_rating_id = ? WHERE id = ?";
    private static final String CLEAR_GENRES_OF_FILM = "DELETE FROM public.\"FILM_GENRES\" WHERE FILM_ID = ?";
    private static final String ADD_GENRE_TO_FILM = "INSERT INTO public.\"FILM_GENRES\" (FILM_ID, GENRE_ID) VALUES(?, ?)";
    private static final String CLEAR_LIKES_OF_FILM = "DELETE FROM public.\"FILM_LIKES\" WHERE FILM_ID = ?";
    private static final String ADD_LIKE_TO_FILM = "INSERT INTO public.\"FILM_LIKES\" (FILM_ID, USER_ID) VALUES(?, ?)";
    private static final String DELETE_FILM = "DELETE FROM public.\"FILM\" WHERE id = ?";
    private static final String SELECT_FILM = "SELECT f.*, r.Name mpa_rating_name FROM public.\"FILM\" f LEFT JOIN public.\"MPA_RATING\" r ON (f.mpa_rating_id = r.Id) WHERE f.id = ?";
    private static final String SELECT_ALL_FILMS = "SELECT f.*, r.Name mpa_rating_name FROM public.\"FILM\" f LEFT JOIN public.\"MPA_RATING\" r ON (f.mpa_rating_id =  r.Id)";
    private static final String SELECT_TOP_FILMS_ORDERED_BY_LIKES = "SELECT f.Id, f.Name, f.Description, f.Release_Date, f.Duration, f.mpa_rating_id, r.Name mpa_rating_name, count(fl.user_id) count_likes \n" +
            "FROM public.\"FILM\" f LEFT JOIN public.\"MPA_RATING\" r ON (f.mpa_rating_id =  r.Id) \n" +
            "LEFT JOIN public.\"FILM_LIKES\" fl ON (f.Id = fl.film_id) \n" +
            "GROUP BY f.Id, f.Name, f.Description, f.Release_Date, f.Duration, r.Name \n" +
            "ORDER BY count_likes desc LIMIT ?";
    private static final String CHECK_IF_FILM_EXISTS = "SELECT count(*) FROM public.\"FILM\" WHERE id = ?";


    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_NEW_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setInt(3, film.getDuration());
            ps.setObject(4, film.getReleaseDate());
            ps.setObject(5, film.getMpaRating() == null ? 1 : film.getMpaRating().getId());
            return ps;
        }, keyHolder);

        // Получаем сгенерированный ID
        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        film.getGenres().forEach(genre -> {
            jdbc.update(INSERT_FILM_GENRES, generatedId, genre.getId());
        });

        List<Genre> genres = jdbc.query(SELECT_GENRES_OF_FILM, genreRowMapper, generatedId);
        film.setGenres(genres);

        MpaRating mpaRating = jdbc.queryForObject(SELECT_MPA_RATING, mpaRowMapper, film.getMpaRating() == null ? 1 : film.getMpaRating().getId());
        film.setMpaRating(mpaRating);

        film.setId(generatedId);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getMpaRating() == null) {
            jdbc.update(UPDATE_FILM_WITHOUT_MPA, film.getName(), film.getDescription(), film.getDuration(), film.getReleaseDate(), film.getId());
        } else {
            jdbc.update(UPDATE_FILM, film.getName(), film.getDescription(), film.getDuration(), film.getReleaseDate(), film.getMpaRating().getId(), film.getId());
        }

        jdbc.update(CLEAR_GENRES_OF_FILM, film.getId());

        if (!CollectionUtils.isEmpty(film.getGenres())) {
            film.getGenres().forEach(genre -> {
                jdbc.update(ADD_GENRE_TO_FILM, film.getId(), genre.getId());
            });
        }

        jdbc.update(CLEAR_LIKES_OF_FILM, film.getId());
        if (!CollectionUtils.isEmpty(film.getUsersWhoLiked())) {
            film.getUsersWhoLiked().forEach(userId -> {
                jdbc.update(ADD_LIKE_TO_FILM, film.getId(), userId);
            });
        }

        return getFilm(film.getId());
    }

    @Override
    public void delete(long id) {
        jdbc.update(CLEAR_GENRES_OF_FILM, id);
        jdbc.update(CLEAR_LIKES_OF_FILM, id);
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
        List<Long> userIds = jdbc.queryForList(SELECT_USER_IDS_WHO_LIKED, Long.class, film.getId());
        film.setUsersWhoLiked(new HashSet<>(userIds));
        return film;
    }

    @Override
    public List<Film> getFilms() {
        List<Film> films = jdbc.query(SELECT_ALL_FILMS, mapper);
        films.forEach(film -> {
            List<Genre> genres = jdbc.query(SELECT_GENRES_OF_FILM, genreRowMapper, film.getId());
            film.setGenres(genres);
            List<Long> userIds = jdbc.queryForList(SELECT_USER_IDS_WHO_LIKED, Long.class, film.getId());
            film.setUsersWhoLiked(new HashSet<>(userIds));
        });
        return films;
    }

    @Override
    public List<Film> getPopularFilms(long count) {
        List<Film> films = jdbc.query(SELECT_TOP_FILMS_ORDERED_BY_LIKES, mapper, count);
        films.forEach(film -> {
            List<Genre> genres = jdbc.query(SELECT_GENRES_OF_FILM, genreRowMapper, film.getId());
            film.setGenres(genres);
            List<Long> userIds = jdbc.queryForList(SELECT_USER_IDS_WHO_LIKED, Long.class, film.getId());
            film.setUsersWhoLiked(new HashSet<>(userIds));
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
