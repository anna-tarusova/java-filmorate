package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User create(@RequestBody User user);

    User update(@RequestBody User user);

    void delete(long id);

    User getUser(long id);

    void ensureUserExists(long id) throws NotFoundException;

    List<User> getUsers();
}
