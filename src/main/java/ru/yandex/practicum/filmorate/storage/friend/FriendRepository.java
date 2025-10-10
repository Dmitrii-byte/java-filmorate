package ru.yandex.practicum.filmorate.storage.friend;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Set;

public interface FriendRepository {
    void addFriend(long userId, long friendId);

    void removeFriend(long userId, long friendId);

    boolean isFriend(long userId, long friendId);

    Set<User> findAllFriends(long userId);

    Set<User> getCommonFriends(long userId1, long userId2);
}