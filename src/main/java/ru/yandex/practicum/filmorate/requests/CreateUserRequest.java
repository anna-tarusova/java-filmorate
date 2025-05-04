package ru.yandex.practicum.filmorate.requests;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateUserRequest {
    String email;
    String login;
    String name;
    LocalDate birthday;
}
