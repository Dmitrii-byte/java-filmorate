package ru.yandex.practicum.filmorate.storage.like;

import java.util.List;

public interface LikeRepository {
    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    int getLikeCount(long filmId);

    boolean hasUserLikedFilm(long filmId, long userId);

    List<Long> getPopularFilmIds(int count);
}
