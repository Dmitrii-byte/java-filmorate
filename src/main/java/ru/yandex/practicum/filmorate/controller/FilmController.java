package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/films")
@Slf4j
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

        log.info("Фильм '{}' (ID: {}) успешно добавлен", film.getName(), film.getId());
        log.debug("Полная информация о добавленном фильме: {}", film);

        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("Ошибка: ID фильма должен быть указан");
            throw new ValidationException("ID фильма должен быть указан");
        }

        if (!films.containsKey(newFilm.getId())) {
            log.warn("Фильм с id {} не найден", newFilm.getId());
            throw new ValidationException("Фильм с id " + newFilm.getId() + " не найден");
        }

        Film oldFilm = films.get(newFilm.getId());
        log.debug("Текущие данные фильма (ID: {}): {}", oldFilm.getId(), oldFilm);

        boolean changed = false;

        if (newFilm.getName() != null && !newFilm.getName().equals(oldFilm.getName())) {
            validateName(newFilm.getName());
            log.info("Изменение названия фильма с ID {}: '{}' -> '{}'",
                    oldFilm.getId(), oldFilm.getName(), newFilm.getName());
            oldFilm.setName(newFilm.getName());
            changed = true;
        }

        if (newFilm.getDescription() != null && !newFilm.getDescription().equals(oldFilm.getDescription())) {
            validateDescription(newFilm.getDescription());
            log.info("Изменение описания фильма с ID {}: '{}' -> '{}'",
                    oldFilm.getId(), oldFilm.getDescription(), newFilm.getDescription());
            oldFilm.setDescription(newFilm.getDescription());
            changed = true;
        }

        if (newFilm.getReleaseDate() != null && !newFilm.getReleaseDate().equals(oldFilm.getReleaseDate())) {
            validateDateRelease(newFilm.getReleaseDate());
            log.info("Изменение даты выхода фильма с ID {}: {} -> {}",
                    oldFilm.getId(), oldFilm.getReleaseDate(), newFilm.getReleaseDate());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            changed = true;
        }

        if (newFilm.getDuration() != null && !newFilm.getDuration().equals(oldFilm.getDuration())) {
            validateDuration(newFilm.getDuration());
            log.info("Изменение продолжительности фильма с ID {}: {} -> {}",
                    oldFilm.getId(), oldFilm.getDuration(), newFilm.getDuration());
            oldFilm.setDuration(newFilm.getDuration());
            changed = true;
        }

        if (changed) {
            log.info("Фильм с ID {} успешно обновлен", oldFilm.getId());
            log.debug("Обновленные данные фильма (ID: {}): {}", oldFilm.getId(), oldFilm);
        } else {
            log.info("Данные фильма с ID {} не изменились", oldFilm.getId());
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
            log.error("Ошибка валидации фильма: название фильма не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }
        log.debug("Валидация названия фильма '{}' прошла успешно", name);
    }

    private void validateDateRelease(LocalDate dataRelease) {
        if (dataRelease.isBefore(validateData)) {
            log.error("Ошибка валидации фильма: Дата релиза {} раньше допустимой {}", dataRelease.format(DateTimeFormatter.ISO_DATE), validateData.format(DateTimeFormatter.ISO_DATE));
            throw new ValidationException("Дата релиза должна быть позже " + validateData.format(DateTimeFormatter.ISO_DATE));
        }
        log.debug("Валидация даты релиза фильма '{}' прошла успешно", dataRelease.format(DateTimeFormatter.ISO_DATE));
    }

    private void validateDuration(Duration duration) {
        if (duration.isNegative() || duration.isZero()) {
            log.error("Ошибка валидации фильма: продолжительность фильма {} должна быть положительной", duration.getSeconds());
            throw new ValidationException("Продолжительность фильма должно быть положительным числом");
        }
        log.debug("Валидация продолжительности фильма {} прошла успешно", duration.getSeconds());
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            log.error("Ошибка валидации фильма: описание фильма не может быть пустым");
            throw new ValidationException("Описание не может быть пустым");
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            log.error("Ошибка валидации фильма: описание слишком длинное ({} символов, максимум {})",
                    description.length(), MAX_DESCRIPTION_LENGTH);
            throw new ValidationException("Описание не может превышать " + MAX_DESCRIPTION_LENGTH + " символов");
        }
        log.debug("Валидация описания фильма ({} символов) прошла успешно", description.length());
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long nextId = ++currentMaxId;
        log.debug("Сгенерирован новый ID для фильма: {}", nextId);
        return nextId;
    }
}
