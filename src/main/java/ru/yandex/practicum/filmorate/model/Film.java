package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    long id;
    String name;
    String description;
    LocalDate releaseDate;
    int duration;
}
