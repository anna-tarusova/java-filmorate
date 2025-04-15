package ru.yandex.practicum.filmorate.model;

import lombok.Getter;

@Getter
public class ErrorResponse {
    // геттеры необходимы, чтобы Spring Boot мог получить значения полей
    // название ошибки
    String error;
    // подробное описание
    String description;

    public ErrorResponse(String error, String description) {
        this.error = error;
        this.description = description;
    }

}