package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final int MAX_DESCRIPTION_LENGTH = 100;
    private final LocalDate MIN_RELEASE_DATE = LocalDate.parse("1895-03-22", DateTimeFormatter.ISO_DATE);

    public void addLikeToFilm(long filmId, long userId) {
        Film film = filmStorage.getFilm(filmId);
        //Проверим что пользователь есть
        userStorage.ensureUserExists(userId);
        film.addUserWhoLiked(userId);
        filmStorage.update(film);
    }

    public void removeLikeToFilm(long filmId, long userId) {
        Film film = filmStorage.getFilm(filmId);
        //Проверим что пользователь есть
        userStorage.ensureUserExists(userId);
        film.deleteUserWhoLiked(userId);
        filmStorage.update(film);
    }

    public List<Film> popularFilms(int count) {
        return filmStorage.getFilms()
                .stream().sorted((a, b) -> Integer.compare(b.countOfLikes(), a.countOfLikes()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms().stream().peek(f -> {
            MpaRating mpaRating = f.getMpaRating();
            f.setMpaRating(mpaRating);
        }).collect(Collectors.toList());
    }

    public Film createFilm(Film film) {
        if (film.getName().isBlank()) {
            throw new ValidationException("Имя фильма не может быть пустым");
        }
        if (film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Слишком длинное описание");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException(String.format("Дата не может быть раньше чем %s", MIN_RELEASE_DATE.format(DateTimeFormatter.ISO_DATE)));
        }
        if (film.getDuration() < 0) {
            throw new ValidationException("Продолжительность не может быть меньше нуля");
        }
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        filmStorage.ensureFilmExists(film.getId());
        return filmStorage.update(film);
    }

    public void delete(long id) {
        filmStorage.delete(id);
    }

    public Film getFilm(long id) {
        return filmStorage.getFilm(id);
    }
}
