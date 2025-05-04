package ru.yandex.practicum.filmorate.service.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    public List<Genre> getMpaRatings() {
        return genreStorage.getAll();
    }

    public Genre getMpaRating(int id) {
        return genreStorage.get(id);
    }

    public void ensureMpaRatingExists(int id) {
        genreStorage.ensureGenreExists(id);
    }
}
