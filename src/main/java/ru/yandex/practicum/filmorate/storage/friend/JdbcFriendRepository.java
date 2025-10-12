package ru.yandex.practicum.filmorate.storage.friend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcFriendRepository implements FriendRepository {
    private final NamedParameterJdbcOperations jdbc;

    @Override
    public void addFriend(long userId, long friendId) {
        log.debug("Добавление пользователя ID {} в друзья пользователя ID {}", friendId, userId);

        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (:user_id, :friend_id)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("user_id", userId);
        params.addValue("friend_id", friendId);

        jdbc.update(sql, params);
        log.info("Пользователь ID {} добавил в друзья пользователя ID {}", userId, friendId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        log.debug("Удаление пользователя ID {} из друзей пользователя ID {}", friendId, userId);

        String sql = "DELETE FROM friends WHERE user_id = :user_id AND friend_id = :friend_id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("user_id", userId);
        params.addValue("friend_id", friendId);

        jdbc.update(sql, params);
        log.info("Пользователь ID {} удалил из друзей пользователя ID {}", userId, friendId);
    }

    @Override
    public boolean isFriend(long userId, long friendId) {
        log.debug("Проверка дружбы между пользователями ID {} и ID {}", userId, friendId);

        String sql = "SELECT COUNT(*) FROM friends WHERE user_id = :user_id AND friend_id = :friend_id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("user_id", userId);
        params.addValue("friend_id", friendId);

        Integer count = jdbc.queryForObject(sql, params, Integer.class);
        boolean isFriend = count != null && count > 0;
        log.debug("Пользователи ID {} и ID {} {} друзья", userId, friendId, isFriend ? "" : "не");
        return isFriend;
    }

    @Override
    public Set<User> findAllFriends(long userId) {
        log.debug("Поиск всех друзей пользователя ID: {}", userId);

        String sql = """
                SELECT u.user_id, u.email, u.login, u.user_name, u.birthday
                FROM users u
                JOIN friends f ON u.user_id = f.friend_id
                WHERE f.user_id = :user_id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("user_id", userId);
        List<User> friends = jdbc.query(sql, params, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("user_id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("user_name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        });

        log.info("Найдено {} друзей у пользователя ID {}", friends.size(), userId);
        return Set.copyOf(friends);
    }

    @Override
    public Set<User> getCommonFriends(long userId1, long userId2) {
        log.debug("Поиск общих друзей пользователей ID {} и ID {}", userId1, userId2);

        String sql = """
                SELECT u.user_id, u.email, u.login, u.user_name, u.birthday
                FROM users u
                JOIN friends f1 ON u.user_id = f1.friend_id
                JOIN friends f2 ON u.user_id = f2.friend_id
                WHERE f1.user_id = :user_id1 AND f2.user_id = :user_id2
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("user_id1", userId1)
                .addValue("user_id2", userId2);

        List<User> commonFriends = jdbc.query(sql, params, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("user_id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("user_name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        });

        log.info("Найдено {} общих друзей между пользователями ID {} и ID {}",
                commonFriends.size(), userId1, userId2);

        return Set.copyOf(commonFriends);
    }
}