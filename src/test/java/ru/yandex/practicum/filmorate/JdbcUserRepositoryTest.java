package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.JdbcUserRepository;
import ru.yandex.practicum.filmorate.storage.user.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({JdbcUserRepository.class, UserRowMapper.class})
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class JdbcUserRepositoryTest {

    private final JdbcUserRepository userRepository;

    @Test
    void testFindById_WhenUserExists_ShouldReturnUser() {
        User user = createTestUser("test@mail.com", "testLogin", "Test User");
        User savedUser = userRepository.create(user);

        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getId()).isEqualTo(savedUser.getId());
                    assertThat(u.getEmail()).isEqualTo("test@mail.com");
                    assertThat(u.getLogin()).isEqualTo("testLogin");
                    assertThat(u.getName()).isEqualTo("Test User");
                });
    }

    @Test
    void testFindById_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> foundUser = userRepository.findById(999L);

        assertThat(foundUser).isEmpty();
    }

    @Test
    void testCreate_ShouldSaveUserAndGenerateId() {
        User user = createTestUser("new@mail.com", "newLogin", "New User");

        User savedUser = userRepository.create(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("new@mail.com");
        assertThat(savedUser.getLogin()).isEqualTo("newLogin");
        assertThat(savedUser.getName()).isEqualTo("New User");
        assertThat(savedUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void testCreate_WhenNameIsBlank_ShouldUseLoginAsName() {
        User user = createTestUser("blank@mail.com", "blankLogin", "");

        User savedUser = userRepository.create(user);

        assertThat(savedUser.getName()).isEqualTo("blankLogin");
    }

    @Test
    void testUpdate_ShouldUpdateUserFields() {
        User user = createTestUser("old@mail.com", "oldLogin", "Old Name");
        User savedUser = userRepository.create(user);

        User updatedUser = createTestUser("new@mail.com", "newLogin", "New Name");
        updatedUser.setId(savedUser.getId());

        User result = userRepository.update(updatedUser);

        assertThat(result.getId()).isEqualTo(savedUser.getId());
        assertThat(result.getEmail()).isEqualTo("new@mail.com");
        assertThat(result.getLogin()).isEqualTo("newLogin");
        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void testFindAll_ShouldReturnAllUsers() {
        User user1 = createTestUser("user1@mail.com", "user1", "User One");
        User user2 = createTestUser("user2@mail.com", "user2", "User Two");

        userRepository.create(user1);
        userRepository.create(user2);

        Collection<User> users = userRepository.findAll();

        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting(User::getLogin)
                .contains("user1", "user2");
    }

    @Test
    void testUpdate_WhenUserNotExists_ShouldThrowException() {
        User nonExistentUser = createTestUser("none@mail.com", "none", "None");
        nonExistentUser.setId(999L);

        org.junit.jupiter.api.Assertions.assertThrows(
                ru.yandex.practicum.filmorate.exception.NotFoundException.class,
                () -> userRepository.update(nonExistentUser)
        );
    }

    @Test
    void testCreateAndFind_IntegrationTest() {
        User user = createTestUser("integ@mail.com", "integLogin", "Integration User");

        User savedUser = userRepository.create(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("integ@mail.com");
        assertThat(foundUser.get().getLogin()).isEqualTo("integLogin");
    }

    private User createTestUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}