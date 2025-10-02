package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Validated
@Slf4j
public class FilmController {
    public final FilmStorage filmStorage;
    public final FilmService filmService;

    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("GET /films - получение всех фильмов");
        return filmStorage.findAll();
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        log.info("POST /films - добавление фильма: {}", film);
        return filmStorage.add(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("PUT /films - обновление фильма: {}", newFilm);
        return filmStorage.update(newFilm);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(
            @PathVariable @Positive(message = "ID фильма должен быть положительным") long filmId,
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") long userId) {
        log.info("PUT /films/{}/like/{} - добавление лайка", filmId, userId);
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(
            @PathVariable @Positive(message = "ID фильма должен быть положительным") long filmId,
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") long userId) {
        log.info("DELETE /films/{}/like/{} - удаление лайка", filmId, userId);
        filmService.removeLike(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10")
            @Positive(message = "Параметр count должен быть положительным числом") int count) {
        log.info("GET /films/popular?count={} - получение популярных фильмов", count);
        return filmService.getPopularFilms(count);
    }
}