package ru.yandex.practicum.filmorate.mappers.genre;

import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

public class GenreMapper {
    public static GenreDto mapGenreToDto(Genre genre) {
        GenreDto dto = new GenreDto();
        dto.setId(genre.getId());
        dto.setName(genre.getName());
        return dto;
    }

    public static Genre mapToGenre(GenreDto genre) {
        Genre dto = new Genre();
        dto.setId(genre.getId());
        dto.setName(genre.getName());
        return dto;
    }
}
