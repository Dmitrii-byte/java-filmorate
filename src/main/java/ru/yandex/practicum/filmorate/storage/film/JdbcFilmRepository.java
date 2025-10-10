package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreRepository;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
@Primary
public class JdbcFilmRepository implements FilmStorage {
    private final NamedParameterJdbcOperations jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRepository genreRepository;
    private final MpaRepository mpaRepository;

    @Override
    public Optional<Film> findById(long id) {
        log.debug("Поиск фильма по ID: {}", id);

        String sql = """
                SELECT f.*, m.mpa_id, m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.mpa_id
                WHERE f.film_id = :film_id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("film_id", id);

        List<Film> films = jdbc.query(sql, params, filmRowMapper);
        if (films.isEmpty()) {
            log.debug("Фильм с ID {} не найден", id);
            return Optional.empty();
        }

        Film film = films.getFirst();

        Set<Genre> genres = genreRepository.findGenresByFilmId(id);
        film.setGenres(genres);

        log.debug("Фильм с ID {} успешно найден: {}", id, film.getName());
        return Optional.of(film);
    }

    @Override
    public Film add(Film film) {
        log.debug("Добавление нового фильма: {}", film.getName());
        validateFilm(film);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", film.getName());
        params.addValue("description", film.getDescription());
        params.addValue("release_date", film.getReleaseDate());
        params.addValue("duration", film.getDuration().toMinutes());
        params.addValue("mpa_id", film.getMpa().getId());

        jdbc.update("""
                INSERT INTO FILMS (name, description, release_date, duration, mpa_id)
                VALUES (:name, :description, :release_date, :duration, :mpa_id)
                """, params, keyHolder, new String[]{"film_id"});

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreRepository.saveFilmGenres(film.getId(), film.getGenres());
        }

        Film savedFilm = findById(film.getId()).orElseThrow(() -> new RuntimeException("Фильм не найден после создания"));
        log.info("Фильм '{}' (ID: {}) успешно создан", savedFilm.getName(), savedFilm.getId());
        return savedFilm;
    }

    @Override
    public Film update(Film film) {
        if (findById(film.getId()).isEmpty())
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        log.debug("Обновление фильма с ID: {}", film.getId());
        validateFilm(film);

        String updateSql = """
                UPDATE FILMS
                SET name = COALESCE(:name, name),
                    description = COALESCE(:description, description),
                    release_date = COALESCE(:release_date, release_date),
                    duration = COALESCE(:duration, duration),
                    mpa_id = COALESCE(:mpa_id, mpa_id)
                WHERE film_id = :film_id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("film_id", film.getId());
        params.addValue("name", film.getName());
        params.addValue("description", film.getDescription());
        params.addValue("release_date", film.getReleaseDate());
        params.addValue("duration", film.getDuration() != null ? film.getDuration().toMinutes() : null);
        params.addValue("mpa_id", film.getMpa() != null ? film.getMpa().getId() : null);

        jdbc.update(updateSql, params);

        if (film.getGenres() != null) {
            genreRepository.updateFilmGenres(film.getId(), film.getGenres());
        }

        Film updatedFilm = findById(film.getId()).orElseThrow(() -> new RuntimeException("Фильм не найден после обновления"));
        log.info("Фильм '{}' (ID: {}) успешно обновлен", updatedFilm.getName(), updatedFilm.getId());
        return updatedFilm;
    }

    @Override
    public List<Film> findAll() {
        log.debug("Получение списка всех фильмов");

        List<Film> films = getAllFilmsWithMpa();

        if (films.isEmpty()) {
            log.debug("В базе данных нет фильмов");
            return films;
        }

        loadGenresForFilms(films);
        return films;
    }

    @Override
    public List<Film> getTopPopular(int count) {
        log.debug("Получение {} популярных фильмов", count);

        String sql = """
                SELECT f.*, m.mpa_id, m.name AS mpa_name,
                       COUNT(l.user_id) as like_count
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.mpa_id
                LEFT JOIN likes l ON f.film_id = l.film_id
                GROUP BY f.film_id, m.mpa_id, m.name
                ORDER BY like_count DESC, f.film_id
                LIMIT :count
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("count", count);

        List<Film> films = jdbc.query(sql, params, filmRowMapper);

        if (!films.isEmpty()) {
            List<Long> filmIds = films.stream()
                    .map(Film::getId)
                    .toList();
            Map<Long, Set<Genre>> filmGenresMap = genreRepository.findGenresForFilms(filmIds);

            films.forEach(film ->
                    film.setGenres(filmGenresMap.getOrDefault(film.getId(), new HashSet<>()))
            );
        }

        log.debug("Найдено {} популярных фильмов", films.size());
        return films;
    }

    private List<Film> getAllFilmsWithMpa() {
        String sql = """
                SELECT f.*, m.mpa_id, m.name AS mpa_name
                FROM films f
                LEFT JOIN MPA m ON f.mpa_id = m.mpa_id;
                """;
        return jdbc.query(sql, filmRowMapper);
    }

    private void loadGenresForFilms(List<Film> films) {
        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .toList();

        Map<Long, Set<Genre>> filmGenresMap = genreRepository.findGenresForFilms(filmIds);

        films.forEach(film ->
                film.setGenres(filmGenresMap.getOrDefault(film.getId(), new HashSet<>()))
        );
    }

    private void validateFilm(Film film) {
        List<String> errors = new ArrayList<>();
        final LocalDate validData = LocalDate.of(1895, 1, 28);


        if (film.getMpa() != null) {
            int mpaId = film.getMpa().getId();
            mpaRepository.findById(mpaId)
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + mpaId + " не найден"));
        }

        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> genreRepository.findById(genre.getId())
                    .orElseThrow(() -> new NotFoundException("Жанр с id " + genre.getId() + " не найден")));
        }

        if (film.getDuration().toMinutes() <= 0) {
            errors.add("Продолжительность не может быть отрицательной или 0");
        }

        if (film.getReleaseDate().isBefore(validData)) {
            errors.add("Дата релиза не может быть раньше" + validData.format(DateTimeFormatter.ISO_DATE));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(String.join("; ", errors));
        }
    }
}