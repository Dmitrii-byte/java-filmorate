package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    public Optional<User> findById(Long id);
    public User update(User newUser);
    public Collection<User> findAll();
    public User create(User user);
}
