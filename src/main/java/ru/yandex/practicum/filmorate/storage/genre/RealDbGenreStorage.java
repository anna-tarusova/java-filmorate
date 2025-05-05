package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.genre.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RealDbGenreStorage implements GenreStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    private final String SELECT_ALL_GENRES = "SELECT * FROM public.\"GENRE\"";
    private final String SELECT_GENRE = "SELECT * FROM public.\"GENRE\" where Id = ?";

    @Override
    public List<Genre> getAll() {
        return jdbc.query(SELECT_ALL_GENRES, mapper);
    }

    @Override
    public Genre get(int id) {
        List<Genre> list = jdbc.query(SELECT_GENRE, mapper, id);
        if (list.isEmpty()) {
            throw new NotFoundException(String.format("Не найден жанр с id=%d", id));
        }
        return list.getFirst();
    }

    @Override
    public void ensureGenreExists(int id) {
        get(id);
    }
}
