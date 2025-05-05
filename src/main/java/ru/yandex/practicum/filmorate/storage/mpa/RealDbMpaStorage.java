package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.mpa.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RealDbMpaStorage implements MpaStorage {
    private final JdbcTemplate jdbc;
    private final MpaRowMapper mapper;

    private static final String SELECT_ALL_RATINGS = "SELECT * FROM public.\"MPA_RATING\"";
    private static final String SELECT_RATING = "SELECT * FROM public.\"MPA_RATING\" where Id = ?";

    @Override
    public List<MpaRating> getAll() {
        return jdbc.query(SELECT_ALL_RATINGS, mapper);
    }

    @Override
    public MpaRating get(int id) {
        List<MpaRating> list = jdbc.query(SELECT_RATING, mapper, id);
        if (list.isEmpty()) {
            throw new NotFoundException(String.format("Не найден mpa-рейтинг с id=%d", id));
        }
        return list.getFirst();
    }

    @Override
    public void ensureMpaRatingExists(int id) {
        get(id);
    }
}
