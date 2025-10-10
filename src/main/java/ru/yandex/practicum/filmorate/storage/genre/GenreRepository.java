package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface GenreRepository {
    Optional<Genre> findById(int id);

    List<Genre> findAll();

    Set<Genre> findGenresByFilmId(long filmId);

    Map<Long, Set<Genre>> findGenresForFilms(List<Long> filmIds);

    void saveFilmGenres(long filmId, Set<Genre> genres);

    void updateFilmGenres(long filmId, Set<Genre> genres);

    void deleteFilmGenres(long filmId);
}