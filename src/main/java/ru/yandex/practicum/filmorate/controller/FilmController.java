package ru.yandex.practicum.filmorate.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.user.FilmService;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> findAll() {
        return this.filmService.getFilms();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        return this.filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        return this.filmService.update(film);
    }

    @DeleteMapping("{id}")
    public void deleteFilm(@PathVariable long id) {
        this.filmService.delete(id);
    }

    @GetMapping("{id}")
    public Film getFilm(@PathVariable long id) {
        return this.filmService.getFilm(id);
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
    public List<Film> getPopularFilms(
            @RequestParam(name = "count", defaultValue = "10")
            @Positive(message = "count не может быть меньше 0")
            Integer count) {
        if (count < 0) {
            throw new ValidationException("Параметр count не может быть меньше 0");
            //аннотации @Positive и @Validation не работают
        }
        return filmService.popularFilms(count);
    }
}
