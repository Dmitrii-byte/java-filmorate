package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/film")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private final LocalDate validateData = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film add(@RequestBody Film film) {
        validateFilm(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("ID фильма должен быть указан");
        }

        if (!films.containsKey(newFilm.getId())) {
            throw new ValidationException("Фильм с id " + newFilm.getId() + " не найден");
        }

        Film oldFilm = films.get(newFilm.getId());

        if (newFilm.getName() != null) {
            validateName(newFilm.getName());
            oldFilm.setName(newFilm.getName());
        }

        if (newFilm.getDescription() != null) {
            validateDescription(newFilm.getDescription());
            oldFilm.setDescription(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null) {
            validateDateRelease(newFilm.getReleaseDate());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() != null) {
            validateDuration(newFilm.getDuration());
            oldFilm.setDuration(newFilm.getDuration());
        }

        return oldFilm;
    }

    private void validateFilm(Film film) {
        validateName(film.getName());
        validateDescription(film.getDescription());
        validateDateRelease(film.getReleaseDate());
        validateDuration(film.getDuration());
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
    }

    private void validateDateRelease(LocalDate dataRelease) {
        if (dataRelease.isBefore(validateData)) {
            throw new ValidationException("Дата релиза должна быть позже " + validateData.format(DateTimeFormatter.ISO_DATE));
        }
    }

    private void validateDuration(Duration duration) {
        if (duration.isNegative()) {
            throw new ValidationException("Продолжительность фильма должно быть положительным числом");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Описание не может превышать " + MAX_DESCRIPTION_LENGTH + " символов");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
