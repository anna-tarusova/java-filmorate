package ru.yandex.practicum.filmorate.mappers.film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));
        MpaRating mpaRating = new MpaRating();
        mpaRating.setId(resultSet.getInt("mpa_rating_id"));
        mpaRating.setName(resultSet.getString("mpa_rating_name"));
        film.setMpaRating(mpaRating);
        return film;
    }
}
