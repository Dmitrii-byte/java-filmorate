package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.mappers.UserRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class JdbcUserRepository implements UserStorage {
    private final NamedParameterJdbcOperations jdbc;
    private final UserRowMapper userRowMapper;

    @Override
    public Optional<User> findById(Long id) {
        log.debug("Поиск пользователя по ID: {}", id);

        String sql = """
                SELECT user_id, email, login, user_name, birthday
                FROM users
                WHERE user_id = :user_id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("user_id", id);

        List<User> users = jdbc.query(sql, params, userRowMapper);
        if (users.isEmpty()) {
            log.debug("Пользователь с ID {} не найден", id);
            return Optional.empty();
        }

        User user = users.getFirst();
        log.debug("Пользователь с ID {} успешно найден: {}", id, user.getLogin());
        return Optional.of(user);
    }

    @Override
    public User create(User user) {
        log.debug("Создание нового пользователя: {}", user.getLogin());

        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя не указано, используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", user.getEmail());
        params.addValue("login", user.getLogin());
        params.addValue("user_name", user.getName());
        params.addValue("birthday", user.getBirthday());

        jdbc.update("""
                INSERT INTO users (email, login, user_name, birthday)
                VALUES (:email, :login, :user_name, :birthday)
                """, params, keyHolder, new String[]{"user_id"});

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        log.info("Пользователь '{}' (ID: {}) успешно создан", user.getLogin(), user.getId());
        log.debug("Полная информация о созданном пользователе: {}", user);

        return user;
    }

    @Override
    public User update(User user) {
        log.debug("Обновление пользователя с ID: {}", user.getId());

        if (user.getId() == null) {
            log.error("Ошибка: ID пользователя должен быть указан");
            throw new ValidationException("ID пользователя должен быть указан");
        }

        if (findById(user.getId()).isEmpty()) {
            log.warn("Пользователь с id {} не найден", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        String updateSql = """
                UPDATE users
                SET email = COALESCE(:email, email),
                    login = COALESCE(:login, login),
                    user_name = COALESCE(:user_name, user_name),
                    birthday = COALESCE(:birthday, birthday)
                WHERE user_id = :user_id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("user_id", user.getId());
        params.addValue("email", user.getEmail());
        params.addValue("login", user.getLogin());
        params.addValue("user_name", user.getName());
        params.addValue("birthday", user.getBirthday());

        jdbc.update(updateSql, params);

        log.info("Пользователь '{}' (ID: {}) успешно обновлен", user.getLogin(), user.getId());
        log.debug("Обновленные данные пользователя: {}", user);

        return user;
    }

    @Override
    public Collection<User> findAll() {
        log.debug("Получение списка всех пользователей");

        String sql = """
                SELECT user_id, email, login, user_name, birthday
                FROM users
                ORDER BY user_id
                """;

        List<User> users = jdbc.query(sql, userRowMapper);
        log.debug("Найдено {} пользователей", users.size());
        return users;
    }
}