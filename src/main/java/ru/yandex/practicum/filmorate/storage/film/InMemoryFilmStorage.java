package ru.yandex.practicum.filmorate.storage.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Qualifier("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private static final Logger log = LoggerFactory.getLogger(InMemoryFilmStorage.class);
    private final Map<Long, Film> films = new HashMap<>();


    @Override
    @PostMapping
    public Film create(@RequestBody Film film) {
        log.debug("Вызван метод create");
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

    private long getNextId() {
        long maxKey = films.keySet().stream()
                .max(Long::compareTo)
                .orElse(0L);

        return maxKey + 1;
    }
}
