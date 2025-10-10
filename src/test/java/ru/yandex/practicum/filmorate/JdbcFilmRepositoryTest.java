package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.JdbcFilmRepository;
import ru.yandex.practicum.filmorate.storage.film.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.genre.JdbcGenreRepository;
import ru.yandex.practicum.filmorate.storage.mpa.JdbcMpaRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@Import({JdbcFilmRepository.class, FilmRowMapper.class, JdbcGenreRepository.class, JdbcMpaRepository.class})
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class JdbcFilmRepositoryTest {

    private final JdbcFilmRepository filmRepository;

    @Test
    void testFindById_WhenFilmExists_ShouldReturnFilm() {
        Film film = createTestFilm("Test Film", "Test Description");
        Film savedFilm = filmRepository.add(film);

        Optional<Film> foundFilm = filmRepository.findById(savedFilm.getId());

        assertThat(foundFilm)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getId()).isEqualTo(savedFilm.getId());
                    assertThat(f.getName()).isEqualTo("Test Film");
                    assertThat(f.getDescription()).isEqualTo("Test Description");
                    assertThat(f.getMpa().getId()).isEqualTo(1);
                });
    }

    @Test
    void testFindById_WhenFilmNotExists_ShouldReturnEmpty() {
        Optional<Film> foundFilm = filmRepository.findById(999L);

        assertThat(foundFilm).isEmpty();
    }

    @Test
    void testAdd_ShouldSaveFilmAndGenerateId() {
        Film film = createTestFilm("New Film", "New Description");

        Film savedFilm = filmRepository.add(film);

        assertThat(savedFilm.getId()).isNotNull();
        assertThat(savedFilm.getName()).isEqualTo("New Film");
        assertThat(savedFilm.getDescription()).isEqualTo("New Description");
        assertThat(savedFilm.getMpa().getId()).isEqualTo(1);
        assertThat(savedFilm.getReleaseDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(savedFilm.getDuration().toMinutes()).isEqualTo(120);
    }

    @Test
    void testAdd_WithGenres_ShouldSaveFilmWithGenres() {
        Film film = createTestFilm("Film with Genres", "Description");
        Genre genre1 = new Genre();
        genre1.setId(1);
        Genre genre2 = new Genre();
        genre2.setId(2);
        film.setGenres(Set.of(genre1, genre2));

        Film savedFilm = filmRepository.add(film);

        Optional<Film> foundFilm = filmRepository.findById(savedFilm.getId());
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getGenres()).hasSize(2);
    }

    @Test
    void testUpdate_ShouldUpdateFilmFields() {
        Film film = createTestFilm("Old Film", "Old Description");
        Film savedFilm = filmRepository.add(film);

        Film updatedFilm = createTestFilm("Updated Film", "Updated Description");
        updatedFilm.setId(savedFilm.getId());
        Mpa newMpa = new Mpa();
        newMpa.setId(2);
        updatedFilm.setMpa(newMpa);

        Film result = filmRepository.update(updatedFilm);

        assertThat(result.getId()).isEqualTo(savedFilm.getId());
        assertThat(result.getName()).isEqualTo("Updated Film");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getMpa().getId()).isEqualTo(2);
    }

    @Test
    void testUpdate_WithGenres_ShouldUpdateFilmGenres() {
        Film film = createTestFilm("Film", "Description");
        Genre genre1 = new Genre();
        genre1.setId(1);
        film.setGenres(Set.of(genre1));
        Film savedFilm = filmRepository.add(film);

        Film updatedFilm = createTestFilm("Updated Film", "Updated Description");
        updatedFilm.setId(savedFilm.getId());
        Genre genre2 = new Genre();
        genre2.setId(2);
        updatedFilm.setGenres(Set.of(genre2));

        Film result = filmRepository.update(updatedFilm);

        Optional<Film> foundFilm = filmRepository.findById(result.getId());
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getGenres()).hasSize(1);
        assertThat(foundFilm.get().getGenres().iterator().next().getId()).isEqualTo(2);
    }

    @Test
    void testFindAll_ShouldReturnAllFilms() {
        Film film1 = createTestFilm("Film One", "Description One");
        Film film2 = createTestFilm("Film Two", "Description Two");

        filmRepository.add(film1);
        filmRepository.add(film2);

        List<Film> films = filmRepository.findAll();

        assertThat(films).hasSize(2);
        assertThat(films)
                .extracting(Film::getName)
                .contains("Film One", "Film Two");
    }

    @Test
    void testUpdate_WhenFilmNotExists_ShouldThrowException() {
        Film nonExistentFilm = createTestFilm("None", "None");
        nonExistentFilm.setId(999L);

        assertThrows(NotFoundException.class, () -> filmRepository.update(nonExistentFilm));
    }

    @Test
    void testAdd_WhenInvalidMpa_ShouldThrowException() {
        Film film = createTestFilm("Film", "Description");
        Mpa invalidMpa = new Mpa();
        invalidMpa.setId(999);
        film.setMpa(invalidMpa);

        assertThrows(NotFoundException.class, () -> filmRepository.add(film));
    }

    @Test
    void testAdd_WhenInvalidGenre_ShouldThrowException() {
        Film film = createTestFilm("Film", "Description");
        Genre invalidGenre = new Genre();
        invalidGenre.setId(999);
        film.setGenres(Set.of(invalidGenre));

        assertThrows(NotFoundException.class, () -> filmRepository.add(film));
    }

    @Test
    void testAdd_WhenInvalidDuration_ShouldThrowException() {
        Film film = createTestFilm("Film", "Description");
        film.setDuration(Duration.ofMinutes(0));

        assertThrows(ValidationException.class, () -> filmRepository.add(film));
    }

    @Test
    void testAdd_WhenInvalidReleaseDate_ShouldThrowException() {
        Film film = createTestFilm("Film", "Description");
        film.setReleaseDate(LocalDate.of(1890, 1, 1));

        assertThrows(ValidationException.class, () -> filmRepository.add(film));
    }

    @Test
    void testGetTopPopular_ShouldReturnFilms() {
        Film film1 = createTestFilm("Popular Film", "Description");
        Film film2 = createTestFilm("Another Film", "Description");

        filmRepository.add(film1);
        filmRepository.add(film2);

        List<Film> popularFilms = filmRepository.getTopPopular(2);

        assertThat(popularFilms).hasSize(2);
    }

    @Test
    void testCreateAndFind_IntegrationTest() {
        Film film = createTestFilm("Integration Film", "Integration Description");

        Film savedFilm = filmRepository.add(film);
        Optional<Film> foundFilm = filmRepository.findById(savedFilm.getId());

        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getName()).isEqualTo("Integration Film");
        assertThat(foundFilm.get().getDescription()).isEqualTo("Integration Description");
    }

    private Film createTestFilm(String name, String description) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);
        return film;
    }
}