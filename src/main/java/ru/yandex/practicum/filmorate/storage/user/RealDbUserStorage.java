package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
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
public class RealDbUserStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    private final String INSERT_USER = "INSERT INTO PUBLIC.\"USER\" (EMAIL, NAME, LOGIN, BIRTHDATE) VALUES(?, ?, ?, ?)";

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
        String deleteFriends = "DELETE PUBLIC.FRIENDSHIP WHERE USER_Id = ?";
        jdbc.update(deleteFriends, user.getId());

        String insertNewFriend = "MERGE INTO PUBLIC.FRIENDSHIP (User_Id, Friend_Id, Is_Approved) VALUES (?, ?, true)";
        String insertNewFriend2 = "MERGE INTO PUBLIC.FRIENDSHIP (Friend_Id, User_Id, Is_Approved) VALUES (?, ?, false)";

        if (user.getFriends() != null) {
            user.getFriends().forEach(friend -> {
                jdbc.update(insertNewFriend, user.getId(), friend);
                jdbc.update(insertNewFriend2, user.getId(), friend);
            });
        }
        return getUser(user.getId());
    }

    @Override
    public void delete(long id) {
        String deleteFromFriendship = "DELETE FROM PUBLIC.FRIENDSHIP WHERE User_id = ?";
        jdbc.update(deleteFromFriendship, id);
        String deleteFromFriendship2 = "DELETE FROM PUBLIC.FRIENDSHIP WHERE Friend_id = ?";
        jdbc.update(deleteFromFriendship2, id);
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
        User user = users.getFirst();
        String friendsQuery = "SELECT Friend_Id FROM public.\"FRIENDSHIP\" where User_id = ? and Is_Approved = true";
        List<Long> friends = jdbc.queryForList(friendsQuery, Long.class, user.getId());
        user.setFriends(new HashSet<>(friends));
        return user;
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
