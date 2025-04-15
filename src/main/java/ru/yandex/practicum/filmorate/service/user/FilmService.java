package ru.yandex.practicum.filmorate.service.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@Service
public class FilmService {
    private FilmStorage filmStorage;
    private UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
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
        return this.filmStorage.getFilms();
    }

    public Film createFilm(Film film) {
        return this.filmStorage.create(film);
    }

    public Film update(Film film) {
        return this.filmStorage.update(film);
    }

    public void delete(long id) {
        this.filmStorage.delete(id);
    }

    public Film getFilm(long id) {
        return this.filmStorage.getFilm(id);
    }
}
