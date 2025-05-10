package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.mappers.mpa.MpaMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;

import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.mappers.mpa.MpaMapper.mapMpaToDto;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaService mpaService;

    @GetMapping
    public List<MpaRatingDto> getAll() {
        return this.mpaService.getAll().stream().map(MpaMapper::mapMpaToDto).collect(Collectors.toList());
    }

    @GetMapping("{id}")
    public MpaRatingDto getMpaRating(@PathVariable int id) {
        MpaRating mpaRating = this.mpaService.getMpaRating(id);
        return mapMpaToDto(mpaRating);
    }
}
