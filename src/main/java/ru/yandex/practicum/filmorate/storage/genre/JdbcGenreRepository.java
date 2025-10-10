package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcGenreRepository implements GenreRepository {
    private final NamedParameterJdbcOperations jdbc;

    @Override
    public Optional<Genre> findById(int id) {
        log.debug("Поиск жанра по ID: {}", id);

        String sql = "SELECT genre_id, name FROM genre WHERE genre_id = :genre_id";
        MapSqlParameterSource params = new MapSqlParameterSource("genre_id", id);

        List<Genre> genres = jdbc.query(sql, params, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        });

        Optional<Genre> result = genres.isEmpty() ? Optional.empty() : Optional.of(genres.getFirst());
        log.debug("Жанр с ID {} {}", id, result.isPresent() ? "найден" : "не найден");
        return result;
    }

    @Override
    public List<Genre> findAll() {
        log.debug("Получение списка всех жанров");

        String sql = "SELECT genre_id, name FROM genre ORDER BY genre_id";
        List<Genre> genres = jdbc.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        });

        log.debug("Найдено {} жанров", genres.size());
        return genres;
    }

    @Override
    public Set<Genre> findGenresByFilmId(long filmId) {
        log.debug("Поиск жанров для фильма ID: {}", filmId);

        String sql = """
                SELECT g.genre_id, g.name AS genre_name
                FROM film_genre fg
                JOIN genre g ON fg.genre_id = g.genre_id
                WHERE fg.film_id = :film_id
                ORDER BY g.genre_id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("film_id", filmId);

        List<Genre> genres = jdbc.query(sql, params, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("genre_name"));
            return genre;
        });

        log.debug("Найдено {} жанров для фильма ID {}", genres.size(), filmId);
        return new LinkedHashSet<>(genres);
    }

    @Override
    public void saveFilmGenres(long filmId, Set<Genre> genres) {
        log.debug("Сохранение {} жанров для фильма ID: {}", genres.size(), filmId);

        if (genres.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (:film_id, :genre_id)";

        List<MapSqlParameterSource> batchParams = genres.stream()
                .map(genre -> new MapSqlParameterSource()
                        .addValue("film_id", filmId)
                        .addValue("genre_id", genre.getId()))
                .toList();

        jdbc.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
        log.info("{} жанров успешно сохранены для фильма ID: {}", genres.size(), filmId);
    }

    @Override
    public void updateFilmGenres(long filmId, Set<Genre> genres) {
        log.debug("Обновление жанров для фильма ID: {}", filmId);

        String deleteSql = "DELETE FROM film_genre WHERE film_id = :film_id";
        jdbc.update(deleteSql, new MapSqlParameterSource("film_id", filmId));

        if (genres != null && !genres.isEmpty()) {
            saveFilmGenres(filmId, genres);
        } else {
            log.debug("Новые жанры не указаны, все жанры удалены для фильма ID: {}", filmId);
        }
    }

    @Override
    public void deleteFilmGenres(long filmId) {
        log.debug("Удаление всех жанров для фильма ID: {}", filmId);

        String sql = "DELETE FROM film_genre WHERE film_id = :film_id";
        MapSqlParameterSource params = new MapSqlParameterSource("film_id", filmId);
        jdbc.update(sql, params);
        log.info("Все жанры удалены для фильма ID: {}", filmId);
    }
}