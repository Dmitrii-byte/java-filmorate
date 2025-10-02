package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final LocalDate validateData = LocalDate.of(1895, 12, 28);

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film add(Film film) {
        validateFilm(film);
        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Фильм '{}' (ID: {}) успешно добавлен", film.getName(), film.getId());
        log.debug("Полная информация о добавленном фильме: {}", film);

        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("Ошибка: ID фильма должен быть указан");
            throw new ValidationException("ID фильма должен быть указан");
        }

        if (!films.containsKey(newFilm.getId())) {
            log.warn("Фильм с id {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден");
        }

        Film oldFilm = films.get(newFilm.getId());
        log.debug("Текущие данные фильма (ID: {}): {}", oldFilm.getId(), oldFilm);

        boolean changed = false;

        if (newFilm.getName() != null && !newFilm.getName().equals(oldFilm.getName())) {
            log.info("Изменение названия фильма с ID {}: '{}' -> '{}'",
                    oldFilm.getId(), oldFilm.getName(), newFilm.getName());
            oldFilm.setName(newFilm.getName());
            changed = true;
        }

        if (newFilm.getDescription() != null && !newFilm.getDescription().equals(oldFilm.getDescription())) {
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

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    private void validateFilm(Film film) {
        validateDateRelease(film.getReleaseDate());
        validateDuration(film.getDuration());
    }

    private void validateDateRelease(LocalDate dataRelease) {
        if (dataRelease.isBefore(validateData)) {
            throw new ValidationException("Дата релиза должна быть позже " + validateData.format(DateTimeFormatter.ISO_DATE));
        }
    }

    private void validateDuration(Duration duration) {
        if (duration.isNegative() || duration.isZero()) {
            throw new ValidationException("Продолжительность фильма должно быть положительным числом");
        }
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