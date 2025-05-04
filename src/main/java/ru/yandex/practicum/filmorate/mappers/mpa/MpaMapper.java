package ru.yandex.practicum.filmorate.mappers.mpa;

import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.MpaRating;

public class MpaMapper {
    public static MpaRatingDto mapMpaToDto(MpaRating rating) {
        if (rating == null) {
            return null;
        }
        MpaRatingDto dto = new MpaRatingDto();
        dto.setId(rating.getId());
        dto.setName(rating.getName());
        return dto;
    }
}
