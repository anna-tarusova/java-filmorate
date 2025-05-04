package ru.yandex.practicum.filmorate.storage.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component
public class InMemoryFilmStorage implements FilmStorage {

    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);


    @Override
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

    @Override
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
        updated.setUsersWhoLiked(film.getUsersWhoLiked());

        return updated;
    }

    @Override
    @DeleteMapping
    public void delete(long id) {
        log.debug("Вызван метод delete");
        films.remove(id);
    }

    public Film getFilm(long id) {
        Film returnedFilm = new Film();
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм не найден");
        }
        Film foundFilm = films.get(id);
        returnedFilm.setId(foundFilm.getId());
        returnedFilm.setName(foundFilm.getName());
        returnedFilm.setDescription(foundFilm.getDescription());
        returnedFilm.setReleaseDate(foundFilm.getReleaseDate());
        returnedFilm.setUsersWhoLiked(foundFilm.getUsersWhoLiked());

        return returnedFilm;
    }

    public List<Film> getFilms() {
        List<Film> result = new ArrayList<>(films.size());
        result.addAll(films.values());
        return result;
    }

    @Override
    public void ensureFilmExists(long id) throws NotFoundException {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм не существует");
        }
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
