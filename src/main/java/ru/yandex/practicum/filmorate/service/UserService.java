package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Set<User> findAllFriends(Long userId) {
        User user = getUserById(userId);
        log.info("Найдено друзей у пользователя ID {}: {}", userId, user.getFriendsId().size());
        return user.getFriendsId().stream()
                .map(this::getUserById)
                .collect(Collectors.toSet());
    }

    public void addFriends(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        log.info("Пользователь ID {} добавил в друзья пользователя ID {}", userId, friendId);
        user.getFriendsId().add(friendId);
        friend.getFriendsId().add(userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        log.info("Пользователь ID {} удалил из друзей пользователя ID {}", userId, friendId);
        user.getFriendsId().remove(friendId);
        friend.getFriendsId().remove(userId);
    }

    public Set<User> getCommonFriends(Long userId1, Long userId2) {
        User user1 = getUserById(userId1);
        User user2 = getUserById(userId2);

        Set<Long> friends1 = user1.getFriendsId();
        Set<Long> friends2 = user2.getFriendsId();

        log.info("Найдены общие друзя между пользователями ID {} и ID {}",
                userId1, userId2);

        return friends1.stream()
                .filter(friends2::contains)
                .map(this::getUserById)
                .collect(Collectors.toSet());
    }

    private User getUserById(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }
}