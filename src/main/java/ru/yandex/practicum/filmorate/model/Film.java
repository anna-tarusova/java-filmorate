package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Film.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    long id;
    String name;
    String description;
    LocalDate releaseDate;
    int duration;
    List<Genre> genres;
    MpaRating mpaRating;
    @JsonIgnore
    Set<Long> usersWhoLiked = new HashSet<>();

    public void addUserWhoLiked(long id) {
        usersWhoLiked.add(id);
    }

    public void deleteUserWhoLiked(long id) {
        usersWhoLiked.remove(id);
    }

    public int countOfLikes() {
        return usersWhoLiked.size();
    }
}
