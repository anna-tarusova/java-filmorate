package ru.yandex.practicum.filmorate.service.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public List<MpaRating> getMpaRatings() {
        return mpaStorage.getAll();
    }

    public MpaRating getMpaRating(int id) {
        return mpaStorage.get(id);
    }

    public void ensureMpaRatingExists(int id) {
        mpaStorage.ensureMpaRatingExists(id);
    }
}
