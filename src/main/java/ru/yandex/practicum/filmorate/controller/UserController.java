package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final static Logger log = LoggerFactory.getLogger(UserController.class);
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.debug("Вызван метод create");
        // проверяем выполнение необходимых условий
        checkUserBeforeAdd(user);
        // формируем дополнительные данные
        user.setId(getNextId());
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        // сохраняем новую публикацию в памяти приложения
        users.put((long) user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        log.debug("Вызван метод update");
        checkUserBeforeAdd(user);
        long id = user.getId();
        User updated = users.get(id);
        updated.setName(user.getName());
        updated.setLogin(user.getLogin());
        updated.setEmail(user.getEmail());
        updated.setBirthday(user.getBirthday());

        return user;
    }

    public void checkUserBeforeAdd(User user) {
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

    private long getNextId() {
        long maxKey = users.keySet().stream()
                .max(Long::compareTo)
                .orElse(0L);

        return maxKey + 1;
    }
}
