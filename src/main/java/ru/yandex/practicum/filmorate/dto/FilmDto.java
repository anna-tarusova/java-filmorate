package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FilmDto {
    long id;
    String name;
    String description;
    LocalDate releaseDate;
    int duration;
    String genreId;
    String mpaRatingId;
}
