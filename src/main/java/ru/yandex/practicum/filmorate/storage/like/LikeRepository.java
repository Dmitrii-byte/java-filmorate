package ru.yandex.practicum.filmorate.storage.like;

public interface LikeRepository {
    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    boolean hasUserLikedFilm(long filmId, long userId);
}
