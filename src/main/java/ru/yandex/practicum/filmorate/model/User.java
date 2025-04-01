package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    long id;
    String email;
    String login;
    String name;
    LocalDate birthday;
}
