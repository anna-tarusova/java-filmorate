package ru.yandex.practicum.filmorate.service.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);

    public FilmService(@Qualifier("RealDbFilmStorage") FilmStorage filmStorage, @Qualifier("RealDbUserStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

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
        checkFilmBeforeAddOrUpdate(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        checkFilmBeforeAddOrUpdate(film);
        filmStorage.ensureFilmExists(film.getId());
        return filmStorage.update(film);
    }

    public void delete(long id) {
        filmStorage.delete(id);
    }

    public Film getFilm(long id) {
        return filmStorage.getFilm(id);
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
}
