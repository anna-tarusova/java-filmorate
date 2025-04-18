package ru.yandex.practicum.filmorate.service.film;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Service
public class UserService {
    private UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriends(long id1, long id2) {
        User user1 = userStorage.getUser(id1);
        User user2 = userStorage.getUser(id2);

        user1.addFriend(user2.getId());
        user2.addFriend(user1.getId());

        userStorage.update(user1);
        userStorage.update(user2);
    }

    public void removeFromFriends(long id1, long id2) {
        User user1 = userStorage.getUser(id1);
        User user2 = userStorage.getUser(id2);

        user1.deleteFriend(user2.getId());
        user2.deleteFriend(user1.getId());

        userStorage.update(user1);
        userStorage.update(user2);
    }

    public List<User> findIntersectionOfFriends(long userId1, long userId2) {
        User user = userStorage.getUser(userId1);
        User user2 = userStorage.getUser(userId2);
        Set<Long> friendsOfUser1 = user.getFriends();
        Set<Long> friendsOfUser2 = user2.getFriends();
        Set<Long> intersection = new HashSet<>(friendsOfUser1);
        intersection.retainAll(friendsOfUser2);
        return intersection.stream().map(id -> userStorage.getUser(id)).toList();
    }

    public List<User> getFriends(long id) {
        User user = userStorage.getUser(id);
        return user.getFriends().stream().map((userId) -> userStorage.getUser(userId)).toList();
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User createUser(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        userStorage.ensureUserExists(user.getId());
        return userStorage.update(user);
    }

    public void deleteUser(long id) {
        userStorage.delete(id);
    }

    public User getUser(long id) {
        return userStorage.getUser(id);
    }
}
