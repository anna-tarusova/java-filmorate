package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film create(@RequestBody Film film);

    Film update(@RequestBody Film film);

    void delete(long id);

    Film getFilm(long id);

    List<Film> getFilms();

    List<Film> getPopularFilms(long count);

    void ensureFilmExists(long id) throws NotFoundException;
}
