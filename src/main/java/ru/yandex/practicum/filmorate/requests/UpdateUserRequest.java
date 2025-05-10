package ru.yandex.practicum.filmorate.requests;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    public long id;
    public String name;
    public String login;
    public String email;
    public LocalDate birthday;
}
