package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeRepository;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeRepository likeRepository;

    public void addLike(Long filmId, Long userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);

        if (likeRepository.hasUserLikedFilm(filmId, userId)) {
            throw new AlreadyExistsException("Пользователь " + userId + " уже поставил лайк фильму " + filmId);
        }

        likeRepository.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);

        if (!likeRepository.hasUserLikedFilm(filmId, userId)) {
            throw new NotFoundException("Пользователь " + userId + " не ставил лайк фильму " + filmId);
        }

        likeRepository.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("count должен быть больше 0: count=" + count);
        }

        return filmStorage.getTopPopular(count);
    }

    private void checkFilmExists(Long filmId) {
        filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
    }

    private void checkUserExists(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }
}