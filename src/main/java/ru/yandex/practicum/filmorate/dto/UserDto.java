package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    long id;
    String email;
    String login;
    String name;
    LocalDate birthday;
}
