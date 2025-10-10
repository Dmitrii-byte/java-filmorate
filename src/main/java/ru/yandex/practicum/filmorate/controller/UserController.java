package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("GET /users - получение всех пользователей");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable @Positive(message = "ID пользователя должен быть положительным") long id) {
        log.info("GET /users/{} - получение пользователя по ID", id);
        return userService.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("POST /users - добавление пользователя: {}", user);
        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("PUT /users - обновление пользователя: {}", user);
        return userService.update(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") long id,
            @PathVariable @Positive(message = "ID друга должен быть положительным") long friendId) {
        log.info("PUT /users/{}/friends/{} - добавление в друзья", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") long id,
            @PathVariable @Positive(message = "ID друга должен быть положительным") long friendId) {
        log.info("DELETE /users/{}/friends/{} - удаление из друзей", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Set<User> findAllFriends(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") long id) {
        log.info("GET /users/{}/friends - получение списка друзей пользователя", id);
        return userService.findAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<User> getCommonFriends(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") long id,
            @PathVariable @Positive(message = "ID другого пользователя должен быть положительным") long otherId) {
        log.info("GET /users/{}/friends/common/{} - поиск общих друзей", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}