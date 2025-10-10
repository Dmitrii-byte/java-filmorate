package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreRepository;
import ru.yandex.practicum.filmorate.storage.like.LikeRepository;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRepository;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeRepository likeRepository;
    private final GenreRepository genreRepository;
    private final MpaRepository mpaRepository;

    public Film add(Film film) {
        validateMpa(film);
        validateGenres(film.getGenres());

        Film savedFilm = filmStorage.add(film);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreRepository.saveFilmGenres(savedFilm.getId(), film.getGenres());
        }

        return loadFilmData(savedFilm);
    }

    public Film update(Film film) {
        checkFilmExists(film.getId());
        validateMpa(film);
        validateGenres(film.getGenres());

        Film updatedFilm = filmStorage.update(film);

        if (film.getGenres() != null) {
            genreRepository.updateFilmGenres(film.getId(), film.getGenres());
        }

        return loadFilmData(updatedFilm);
    }

    public Optional<Film> findById(long id) {
        Optional<Film> filmOpt = filmStorage.findById(id);
        filmOpt.ifPresent(this::loadFilmData);
        return filmOpt;
    }

    public List<Film> findAll() {
        List<Film> films = filmStorage.findAll();
        films.forEach(this::loadFilmData);
        return films;
    }

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

        List<Film> films = filmStorage.getTopPopular(count);
        films.forEach(this::loadFilmData);
        return films;
    }

    private Film loadFilmData(Film film) {
        Set<Genre> genres = genreRepository.findGenresByFilmId(film.getId());
        film.setGenres(genres);
        return film;
    }

    private void validateMpa(Film film) {
        if (film.getMpa() != null) {
            int mpaId = film.getMpa().getId();
            mpaRepository.findById(mpaId)
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + mpaId + " не найден"));
        }
    }

    private void validateGenres(Set<Genre> genres) {
        if (genres != null) {
            genres.forEach(genre -> genreRepository.findById(genre.getId())
                    .orElseThrow(() -> new NotFoundException("Жанр с id " + genre.getId() + " не найден")));
        }
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