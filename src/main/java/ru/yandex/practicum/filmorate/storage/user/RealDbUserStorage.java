package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.user.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Qualifier("RealDbUserStorage")
public class RealDbUserStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    private static final String INSERT_USER = "INSERT INTO public.\"USER\" (EMAIL, NAME, LOGIN, BIRTHDATE) VALUES(?, ?, ?, ?)";
    private static final String UPDATE_USER = "UPDATE public.\"USER\" SET email = ?, name = ?, login = ?, birthdate = ? WHERE id = ?";
    private static final String DELETE_USER = "DELETE FROM public.\"USER\" WHERE id = ?";
    private static final String SELECT_USER = "SELECT * FROM public.\"USER\" WHERE id = ?";
    private static final String DELETE_FRIENDS = "DELETE PUBLIC.FRIENDSHIP WHERE USER_Id = ?";
    private static final String ADD_NEW_FRIEND = "MERGE INTO PUBLIC.FRIENDSHIP (User_Id, Friend_Id, Is_Approved) VALUES (?, ?, true)";
    private static final String REQUEST_TO_BE_FRIEND = "MERGE INTO PUBLIC.FRIENDSHIP (Friend_Id, User_Id, Is_Approved) VALUES (?, ?, false)";
    private static final String DELETE_REQUESTS_TO_FRIENDS = "DELETE FROM PUBLIC.FRIENDSHIP WHERE Friend_id = ?";
    private static final String SELECT_FRIENDS = "SELECT Friend_Id FROM public.FRIENDSHIP where User_id = ? and Is_Approved = true";
    private static final String CHECK_IF_USER_EXISTS = "SELECT count(*) FROM public.\"USER\" WHERE id = ?";
    private static final String SELECT_ALL_USERS = "SELECT * FROM public.\"USER\"";

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS);
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
        jdbc.update(UPDATE_USER, user.getEmail(), user.getName(), user.getLogin(), user.getBirthday(), user.getId());
        jdbc.update(DELETE_FRIENDS, user.getId());

        if (!CollectionUtils.isEmpty(user.getFriends())) {
            user.getFriends().forEach(friend -> {
                jdbc.update(ADD_NEW_FRIEND, user.getId(), friend);
                jdbc.update(REQUEST_TO_BE_FRIEND, user.getId(), friend);
            });
        }
        return getUser(user.getId());
    }

    @Override
    public void delete(long id) {
        jdbc.update(DELETE_FRIENDS, id);
        jdbc.update(DELETE_REQUESTS_TO_FRIENDS, id);
        jdbc.update(DELETE_USER, id);
    }

    @Override
    public User getUser(long id) {
        List<User> users = jdbc.query(SELECT_USER, mapper, id);
        if ((long) users.size() == 0) {
            throw new NotFoundException(String.format("Пользователь с id=%d не найден", id));
        }
        User user = users.getFirst();
        List<Long> friends = jdbc.queryForList(SELECT_FRIENDS, Long.class, user.getId());
        user.setFriends(new HashSet<>(friends));
        return user;
    }

    @Override
    public void ensureUserExists(long id) throws NotFoundException {
        long count = jdbc.queryForObject(CHECK_IF_USER_EXISTS, Long.class, id);
        if (count == 0) {
            throw new NotFoundException(String.format("User с id=%d не найден", id));
        }
    }

    @Override
    public List<User> getUsers() {
        return jdbc.query(SELECT_ALL_USERS, mapper);
    }
}
