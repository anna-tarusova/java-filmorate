package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.film.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.requests.CreateFilmRequest;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import javax.validation.constraints.Positive;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.mappers.film.FilmMapper.mapToFilmDto;

@RestController
@RequestMapping("/films")
@Validated
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;
    private final MpaService mpaService;
    private final GenreStorage genreStorage;

    @GetMapping
    public List<FilmDto> findAll() {
        return this.filmService.getFilms().stream().map(FilmMapper::mapToFilmDto).collect(Collectors.toList());
    }

    @PostMapping
    public FilmDto addFilm(@RequestBody CreateFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (request.getGenres() != null) {
            request.getGenres().forEach(genreRequest -> {
                genreStorage.ensureGenreExists(genreRequest.getId());
            });
        }

        if (request.getMpa() != null) {
            mpaService.ensureMpaRatingExists(request.getMpa().getId());
            MpaRating mpaRating = new MpaRating();
            mpaRating.setId(request.getMpa().getId());
            film.setMpaRating(mpaRating);
        }

        if (request.getGenres() == null) {
            film.setGenres(List.of());
        }
        else {
            Set<Integer> ids = new HashSet<>();
            film.setGenres(request.getGenres().stream().filter(genreRequest -> {
                if (ids.contains(genreRequest.getId())) {
                    return false;
                }
                ids.add(genreRequest.getId());
                return true;
            }).map(genreRequest -> {
                Genre genre = new Genre();
                genre.setId(genreRequest.getId());
                return genre;
            }).collect(Collectors.toList()));
        }

        return mapToFilmDto(this.filmService.createFilm(film));
    }

    @PutMapping
    public FilmDto updateFilm(@RequestBody Film film) {
        return mapToFilmDto(this.filmService.update(film));
    }

    @DeleteMapping("{id}")
    public void deleteFilm(@PathVariable long id) {
        this.filmService.delete(id);
    }

    @GetMapping("{id}")
    public FilmDto getFilm(@PathVariable long id) {
        Film film = this.filmService.getFilm(id);
        return mapToFilmDto(film);
    }

    @PutMapping("{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        filmService.addLikeToFilm(id, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        filmService.removeLikeToFilm(id, userId);
    }

    @GetMapping("popular")
    public List<FilmDto> getPopularFilms(
            @RequestParam(name = "count", defaultValue = "10")
            @Positive(message = "count не может быть меньше 0")
            Integer count) {
        if (count < 0) {
            throw new ValidationException("Параметр count не может быть меньше 0");
            //аннотации @Positive и @Validation не работают
        }
        return filmService.popularFilms(count)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }
}
