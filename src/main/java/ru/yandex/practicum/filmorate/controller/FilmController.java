package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.debug("Вызван метод create");
        checkFilmBeforeAddOrUpdate(film);
        // формируем дополнительные данные
        film.setId(this.getNextId());
        // сохраняем новую публикацию в памяти приложения
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.debug("Вызван метод update");
        checkFilmBeforeAddOrUpdate(film);
        long id = film.getId();
        Film updated = films.get(id);
        updated.setName(film.getName());
        updated.setDuration(film.getDuration());
        updated.setReleaseDate(film.getReleaseDate());
        updated.setDescription(film.getDescription());

        return updated;
    }

    public static void checkFilmBeforeAddOrUpdate(Film film) {
        if (film.getName().isEmpty()
                || (film.getDescription() != null && film.getDescription().length() > 200)
                || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)
                || film.getDuration() < 0) {
            log.error("Валидация не пройдена name={}, description={}, releaseDate={}, duration={}",
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate().format(DateTimeFormatter.ISO_DATE),
                    film.getDuration());
            throw new ValidationException("Введенные данные не соответствуют критериям для добавления нового фильма");
        }
    }

    private long getNextId() {
        long maxKey = films.keySet().stream()
                .max(Long::compareTo)
                .orElse(0L);

        return maxKey + 1;
    }
}
