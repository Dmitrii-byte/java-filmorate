package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendRepository;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendRepository friendRepository;

    public Set<User> findAllFriends(Long userId) {
        checkUserExists(userId);
        return friendRepository.findAllFriends(userId);
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }

        checkUserExists(userId);
        checkUserExists(friendId);

        if (friendRepository.isFriend(userId, friendId)) {
            throw new AlreadyExistsException("Пользователь " + userId + " уже добавил в друзья пользователя " + friendId);
        }

        friendRepository.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);

        friendRepository.removeFriend(userId, friendId);
    }

    public Set<User> getCommonFriends(Long userId1, Long userId2) {
        checkUserExists(userId1);
        checkUserExists(userId2);

        return friendRepository.getCommonFriends(userId1, userId2);
    }

    private void checkUserExists(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }
}