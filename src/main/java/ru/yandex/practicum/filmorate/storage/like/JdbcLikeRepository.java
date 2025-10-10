package ru.yandex.practicum.filmorate.storage.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcLikeRepository implements LikeRepository {
    private final NamedParameterJdbcOperations jdbc;

    @Override
    public void addLike(long filmId, long userId) {
        log.debug("Добавление лайка от пользователя ID {} фильму ID {}", userId, filmId);

        String sql = "INSERT INTO LIKES (film_id, user_id) VALUES (:film_id, :user_id)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("film_id", filmId);
        params.addValue("user_id", userId);

        jdbc.update(sql, params);
        log.info("Пользователь ID {} поставил лайк фильму ID {}", userId, filmId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        log.debug("Удаление лайка от пользователя ID {} фильму ID {}", userId, filmId);

        String sql = "DELETE FROM LIKES WHERE film_id = :film_id AND user_id = :user_id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("film_id", filmId);
        params.addValue("user_id", userId);

        jdbc.update(sql, params);
        log.info("Пользователь ID {} удалил лайк с фильма ID {}", userId, filmId);
    }

    @Override
    public int getLikeCount(long filmId) {
        log.debug("Получение количества лайков для фильма ID: {}", filmId);

        String sql = "SELECT COUNT(*) FROM LIKES WHERE film_id = :film_id";
        MapSqlParameterSource params = new MapSqlParameterSource("film_id", filmId);

        Integer count = jdbc.queryForObject(sql, params, Integer.class);
        int result = count != null ? count : 0;
        log.debug("Фильм ID {} имеет {} лайков", filmId, result);
        return result;
    }

    @Override
    public boolean hasUserLikedFilm(long filmId, long userId) {
        log.debug("Проверка лайка пользователя ID {} фильму ID {}", userId, filmId);

        String sql = "SELECT COUNT(*) FROM LIKES WHERE film_id = :film_id AND user_id = :user_id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("film_id", filmId);
        params.addValue("user_id", userId);

        Integer count = jdbc.queryForObject(sql, params, Integer.class);
        boolean hasLiked = count != null && count > 0;
        log.debug("Пользователь ID {} {} лайкнул фильм ID {}", userId, hasLiked ? "" : "не", filmId);
        return hasLiked;
    }

    public List<Long> getPopularFilmIds(int count) {
        log.debug("Получение {} популярных ID фильмов", count);

        String sql = """
                SELECT film_id, COUNT(user_id) as like_count
                FROM LIKES
                GROUP BY film_id
                ORDER BY like_count DESC
                LIMIT :count
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("count", count);

        List<Long> popularIds = jdbc.query(sql, params, (rs, rowNum) -> rs.getLong("film_id"));
        log.debug("Найдено {} популярных ID фильмов", popularIds.size());
        return popularIds;
    }
}