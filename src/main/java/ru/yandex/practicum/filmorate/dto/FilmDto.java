package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilmDto {
    long id;
    String name;
    String description;
    LocalDate releaseDate;
    int duration;
    List<GenreDto> genres;
    @JsonProperty("mpa")
    MpaRatingDto mpaRating;
}
