package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreRepository;

import java.util.List;

@RestController
@RequestMapping("/genres")
@Validated
@Slf4j
@RequiredArgsConstructor
public class GenreController {
    private final GenreRepository genreRepository;

    @GetMapping
    public List<Genre> findAll() {
        log.info("GET /genres - получение всех жанров");
        return genreRepository.findAll();
    }

    @GetMapping("/{id}")
    public Genre findById(@PathVariable int id) {
        log.info("GET /genres/{} - получение жанра по ID", id);
        return genreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
    }
}