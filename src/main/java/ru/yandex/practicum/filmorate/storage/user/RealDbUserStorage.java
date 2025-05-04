package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.user.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class RealDbUserStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    @Override
    public User create(User user) {
        String sql = "INSERT INTO PUBLIC.\"USER\"\n" +
                "(EMAIL, NAME, LOGIN, BIRTHDATE) VALUES(?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setObject(2, user.getName());
            ps.setObject(3, user.getLogin());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);

        // Получаем сгенерированный ID
        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(generatedId);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE public.\"USER\" SET email = ?, name = ?, login = ?, birthdate = ? WHERE id = ?";
        jdbc.update(sql, user.getEmail(), user.getName(), user.getLogin(), user.getBirthday(), user.getId());
        //теоретически может быть триггер, который изменит данные, поэтому достаем из бд
        //или данные могу не обновиться из-за констрэйнтов
        return getUser(user.getId());
    }

    @Override
    public void delete(long id) {
        String query = "DELETE FROM public.\"USER\" WHERE id = ?";
        jdbc.update(query, id);
    }

    @Override
    public User getUser(long id) {
        String query = "SELECT * FROM public.\"USER\" WHERE id = ?";
        List<User> users = jdbc.query(query, mapper, id);
        if ((long) users.size() == 0) {
            throw new NotFoundException(String.format("Пользователь с id=%d не найден", id));
        }
        return users.getFirst();
    }

    @Override
    public void ensureUserExists(long id) throws NotFoundException {
        String query = "SELECT count(*) FROM public.\"USER\" WHERE id = ?";
        long count = jdbc.queryForObject(query, Long.class, id);
        if (count == 0) {
            throw new NotFoundException(String.format("User с id=%d не найден", id));
        }
    }

    @Override
    public List<User> getUsers() {
        String query = "SELECT * FROM public.\"USER\"";
        return jdbc.query(query, mapper);
    }
}
