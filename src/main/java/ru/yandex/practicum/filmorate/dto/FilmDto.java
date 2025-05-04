package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
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
