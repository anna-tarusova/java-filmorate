package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final UserStorage userStorage;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(@Qualifier("RealDbUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriends(long id1, long id2) {
        User user1 = userStorage.getUser(id1);
        userStorage.ensureUserExists(id2);

        user1.addFriend(id2);

        userStorage.update(user1);
    }

    public void removeFromFriends(long id1, long id2) {
        User user1 = userStorage.getUser(id1);
        userStorage.ensureUserExists(id2);

        user1.deleteFriend(id2);

        userStorage.update(user1);
    }

    public List<User> findIntersectionOfFriends(long userId1, long userId2) {
        User user = userStorage.getUser(userId1);
        User user2 = userStorage.getUser(userId2);
        Set<Long> friendsOfUser1 = user.getFriends();
        Set<Long> friendsOfUser2 = user2.getFriends();
        Set<Long> intersection = new HashSet<>(friendsOfUser1);
        intersection.retainAll(friendsOfUser2);
        return intersection.stream().map(userStorage::getUser).toList();
    }

    public List<User> getFriends(long id) {
        User user = userStorage.getUser(id);
        return user.getFriends().stream().map(userStorage::getUser).toList();
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User createUser(User user) {
        checkUserBeforeAddOrUpdate(user);
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        checkUserBeforeAddOrUpdate(user);
        userStorage.ensureUserExists(user.getId());
        return userStorage.update(user);
    }

    public void deleteUser(long id) {
        userStorage.delete(id);
    }

    public User getUser(long id) {
        return userStorage.getUser(id);
    }

    public void checkUserBeforeAddOrUpdate(User user) {
        LocalDate currentDate = LocalDate.now();

        if (user.getEmail().isBlank()
                || !user.getEmail().contains("@")
                || user.getLogin().isBlank() || user.getLogin().contains(" ")
                || user.getBirthday().isAfter(currentDate)) {
            log.error("Валидация не пройдена email={}, login={}, birthDate={}",
                    user.getEmail(),
                    user.getLogin(),
                    user.getBirthday().format(DateTimeFormatter.ISO_DATE));
            throw new ValidationException("Введенные данные не соответствуют критериям для добавления нового пользовате" +
                    "ля");
        }
    }
}
