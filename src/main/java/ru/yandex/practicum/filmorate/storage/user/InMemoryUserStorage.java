package ru.yandex.practicum.filmorate.storage.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Qualifier("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(@RequestBody User user) {
        log.debug("Вызван метод create");
        // формируем дополнительные данные
        user.setId(getNextId());
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        // сохраняем новую публикацию в памяти приложения
        users.put((long) user.getId(), user);
        return user;
    }

    @Override
    public User update(@RequestBody User user) {
        log.debug("Вызван метод update");
        long id = user.getId();
        User updated = users.get(id);
        updated.setName(user.getName());
        updated.setLogin(user.getLogin());
        updated.setEmail(user.getEmail());
        updated.setBirthday(user.getBirthday());
        updated.setFriends(user.getFriends());

        return user;
    }

    @Override
    public void delete(long id) {
        log.debug("Вызван метод delete");
        users.remove(id);
    }

    public User getUser(long id) {
        User returnedUser = new User();
        ensureUserExists(id);
        User foundUser = users.get(id);
        returnedUser.setId(foundUser.getId());
        returnedUser.setLogin(foundUser.getLogin());
        returnedUser.setEmail(foundUser.getEmail());
        returnedUser.setBirthday(foundUser.getBirthday());
        returnedUser.setFriends(foundUser.getFriends());
        return returnedUser;
    }

    @Override
    public void ensureUserExists(long id) throws NotFoundException {
        if (!users.keySet().contains(id)) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    public List<User> getUsers() {
        return users.keySet().stream().map(id -> getUser(id)).toList();
    }

    private long getNextId() {
        long maxKey = users.keySet().stream()
                .max(Long::compareTo)
                .orElse(0L);

        return maxKey + 1;
    }
}
