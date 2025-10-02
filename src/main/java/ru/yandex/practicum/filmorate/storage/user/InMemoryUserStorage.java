package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        if (user.getName() == null) {
            log.info("Имя пользователя не указано, используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);

        log.info("Пользователь '{}' (ID: {}) успешно создан", user.getLogin(), user.getId());
        log.debug("Полная информация о созданном пользователе: {}", user);

        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            log.error("Ошибка: ID пользователя должен быть указан");
            throw new ValidationException("ID пользователя должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            log.warn("Пользователь с id {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id " + newUser.getId() + " не найден");
        }

        User oldUser = users.get(newUser.getId());
        log.debug("Текущие данные пользователя (ID: {}): {}", oldUser.getId(), oldUser);

        boolean changed = false;

        if (newUser.getName() != null && !newUser.getName().equals(oldUser.getName())) {
            log.info("Изменение имени пользователя с ID {}: '{}' -> '{}'",
                    oldUser.getId(), oldUser.getName(), newUser.getName());
            oldUser.setName(newUser.getName());
            changed = true;
        }

        if (newUser.getEmail() != null && !newUser.getEmail().equals(oldUser.getEmail())) {
            log.info("Изменение email пользователя с ID {}: '{}' -> '{}'",
                    oldUser.getId(), oldUser.getEmail(), newUser.getEmail());
            oldUser.setEmail(newUser.getEmail());
            changed = true;
        }

        if (newUser.getLogin() != null && !newUser.getLogin().equals(oldUser.getLogin())) {
            log.info("Изменение логина пользователя с ID {}: '{}' -> '{}'",
                    oldUser.getId(), oldUser.getLogin(), newUser.getLogin());
            oldUser.setLogin(newUser.getLogin());
            changed = true;
        }

        if (newUser.getBirthday() != null && !newUser.getBirthday().equals(oldUser.getBirthday())) {
            log.info("Изменение даты рождения пользователя с ID {}: {} -> {}",
                    oldUser.getId(), oldUser.getBirthday(), newUser.getBirthday());
            oldUser.setBirthday(newUser.getBirthday());
            changed = true;
        }

        if (changed) {
            log.info("Пользователь с ID {} успешно обновлен", oldUser.getId());
            log.debug("Обновленные данные пользователя (ID: {}): {}", oldUser.getId(), oldUser);
        } else {
            log.info("Данные пользователя с ID {} не изменились", oldUser.getId());
        }

        return oldUser;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long nextId = ++currentMaxId;
        log.debug("Сгенерирован новый ID для пользователя: {}", nextId);
        return nextId;
    }
}