package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.mappers.genre.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.genre.GenreService;

import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.mappers.genre.GenreMapper.mapGenreToDto;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public List<GenreDto> getAll() {
        return this.genreService.getAll().stream().map(GenreMapper::mapGenreToDto).collect(Collectors.toList());
    }

    @GetMapping("{id}")
    public GenreDto getGenre(@PathVariable int id) {
        Genre genre = this.genreService.getGenre(id);
        return mapGenreToDto(genre);
    }
}
