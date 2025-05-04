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

    @Override
    public List<MpaRating> getAll() {
        String query = "SELECT * FROM public.\"MPA_RATING\"";
        return jdbc.query(query, mapper);
    }

    @Override
    public MpaRating get(int id) {
        String query = "SELECT * FROM public.\"MPA_RATING\" where Id = ?";
        List<MpaRating> list = jdbc.query(query, mapper, id);
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
