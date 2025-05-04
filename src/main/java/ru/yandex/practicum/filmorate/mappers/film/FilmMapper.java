package ru.yandex.practicum.filmorate.mappers.film;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.mappers.genre.GenreMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.mappers.mpa.MpaMapper.mapMpaToDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {
    public static FilmDto mapToFilmDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        if (film.getGenres() != null) {
            dto.setGenres(film.getGenres().stream().map(GenreMapper::mapGenreToDto).collect(Collectors.toList()));
        }

        dto.setMpaRating(mapMpaToDto(film.getMpaRating()));
        return dto;
    }

    public static Film mapToFilm(FilmDto dto) {
        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());
        //film.setGenreId(dto.getGenreId());
        //film.setMpaRatingId(dto.getMpaRatingId());
        return film;
    }
}
