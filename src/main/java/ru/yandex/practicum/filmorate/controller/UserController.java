package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private final LocalDate now = LocalDate.now();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validateUser(user);
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

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.error("Ошибка: ID пользователя должен быть указан");
            throw new ValidationException("ID пользователя должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            log.warn("Пользователь с id {} не найден", newUser.getId());
            throw new ValidationException("Пользователь с id " + newUser.getId() + " не найден");
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
            validateEmail(newUser.getEmail());
            log.info("Изменение email пользователя с ID {}: '{}' -> '{}'",
                    oldUser.getId(), oldUser.getEmail(), newUser.getEmail());
            oldUser.setEmail(newUser.getEmail());
            changed = true;
        }

        if (newUser.getLogin() != null && !newUser.getLogin().equals(oldUser.getLogin())) {
            validateLogin(newUser.getLogin());
            log.info("Изменение логина пользователя с ID {}: '{}' -> '{}'",
                    oldUser.getId(), oldUser.getLogin(), newUser.getLogin());
            oldUser.setLogin(newUser.getLogin());
            changed = true;
        }

        if (newUser.getBirthday() != null && !newUser.getBirthday().equals(oldUser.getBirthday())) {
            validateData(newUser.getBirthday());
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

    private void validateUser(User user) {
        validateEmail(user.getEmail());
        validateLogin(user.getLogin());
        validateData(user.getBirthday());
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            log.error("Ошибка валидации пользователя: почта пользователя не может быть пустой");
            throw new ValidationException("Почта не может быть пустой");
        }

        if (!email.contains("@")) {
            log.error("Ошибка валидации пользователя: почта '{}' не содержит символ @", email);
            throw new ValidationException("Почто должна иметь символ \"@\"");
        }
        log.debug("Валидация email '{}' пользователя прошла успешно", email);
    }

    private void validateLogin(String login) {
        if (login == null || login.isBlank()) {
            log.error("Ошибка валидации пользователя: логин пользователя не может быть пустым");
            throw new ValidationException("логин не может быть пустым");
        }
        if (login.contains(" ")) {
            log.error("Ошибка валидации пользователя: логин '{}' содержит пробелы", login);
            throw new ValidationException("Логин не может содержать пробелы");
        }
        log.debug("Валидация логина '{}' пользователя прошла успешно", login);
    }

    private void validateData(LocalDate data) {
        if (data.isAfter(now)) {
            log.error("Ошибка валидации пользователя: дата рождения {} не может быть в будущем", data);
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        log.debug("Валидация даты рождения {} пользователя прошла успешно", data);
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
