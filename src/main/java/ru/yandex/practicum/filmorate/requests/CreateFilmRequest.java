package ru.yandex.practicum.filmorate.requests;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateFilmRequest {
    long id;
    String name;
    String description;
    LocalDate releaseDate;
    int duration;
    MpaRequest mpa;
    List<GenreRequest> genres;
}
