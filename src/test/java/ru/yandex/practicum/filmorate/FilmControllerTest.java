package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {

    @Autowired
    private FilmController filmController;

    @Test
    void addFilm_WithValidData_ShouldAddFilmSuccessfully() {
        Film film = createValidFilm();

        Film result = filmController.add(film);

        assertNotNull(result.getId());
        assertEquals("Test Film", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(LocalDate.of(2020, 1, 1), result.getReleaseDate());
        assertEquals(Duration.ofMinutes(120), result.getDuration());
    }

    @Test
    void addFilm_WithEmptyName_ShouldThrowValidationException() {
        Film film = createValidFilm();
        film.setName("");

        assertThrows(ConstraintViolationException.class, () -> filmController.add(film));
    }

    @Test
    void addFilm_WithNullName_ShouldThrowValidationException() {
        Film film = createValidFilm();
        film.setName(null);

        assertThrows(ConstraintViolationException.class, () -> filmController.add(film));
    }

    @Test
    void addFilm_WithTooLongDescription_ShouldThrowValidationException() {
        Film film = createValidFilm();
        film.setDescription("A".repeat(201));

        assertThrows(ConstraintViolationException.class, () -> filmController.add(film));
    }

    @Test
    void addFilm_WithEmptyDescription_ShouldThrowValidationException() {
        Film film = createValidFilm();
        film.setDescription("");

        assertThrows(ConstraintViolationException.class, () -> filmController.add(film));
    }

    @Test
    void addFilm_WithEarlyReleaseDate_ShouldThrowValidationException() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(Exception.class, () -> filmController.add(film));
    }

    @Test
    void addFilm_WithNegativeDuration_ShouldThrowValidationException() {
        Film film = createValidFilm();
        film.setDuration(Duration.ofMinutes(-10));

        assertThrows(Exception.class, () -> filmController.add(film));
    }

    @Test
    void addFilm_WithZeroDuration_ShouldThrowValidationException() {
        Film film = createValidFilm();
        film.setDuration(Duration.ZERO);

        assertThrows(Exception.class, () -> filmController.add(film));
    }

    @Test
    void updateFilm_WithNonExistentId_ShouldThrowValidationException() {
        Film film = createValidFilm();
        film.setId(999L);

        assertThrows(NotFoundException.class, () -> filmController.update(film));
    }

    @Test
    void updateFilm_WithNullId_ShouldThrowValidationException() {
        Film film = createValidFilm();
        film.setId(null);

        assertThrows(ValidationException.class, () -> filmController.update(film));
    }

    @Test
    void findAll_WhenNoFilms_ShouldReturnEmptyCollection() {
        filmController.findAll().clear();
        assertTrue(filmController.findAll().isEmpty());
    }

    @Test
    void findAll_WhenFilmsExist_ShouldReturnAllFilms() {
        Film film1 = filmController.add(createValidFilm());
        Film film2 = filmController.add(createValidFilm());
        film2.setName("Another Film");

        var result = filmController.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(film1));
        assertTrue(result.contains(film2));
    }

    @Test
    void updateFilm_WithValidData_ShouldUpdateFilmSuccessfully() {
        Film originalFilm = filmController.add(createValidFilm());
        Film updatedFilm = createValidFilm();
        updatedFilm.setId(originalFilm.getId());
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated Description");

        Film result = filmController.update(updatedFilm);

        assertEquals("Updated Film", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(originalFilm.getId(), result.getId());
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        return film;
    }
}