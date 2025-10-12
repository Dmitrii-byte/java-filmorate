package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    public List<Film> findAll();

    public Film add(Film film);

    public Film update(Film newFilm);

    public Optional<Film> findById(long id);

    List<Film> getTopPopular(int count);
}
