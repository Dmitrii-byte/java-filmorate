package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void createUser_WithValidData_ShouldCreateUserSuccessfully() {
        User user = createValidUser();

        User result = userController.create(user);

        assertNotNull(result.getId());
        assertEquals("user@example.com", result.getEmail());
        assertEquals("login", result.getLogin());
        assertEquals("User Name", result.getName());
        assertEquals(LocalDate.of(2000, 1, 1), result.getBirthday());
    }

    @Test
    void createUser_WithNullName_ShouldUseLoginAsName() {
        User user = createValidUser();
        user.setName(null);

        User result = userController.create(user);

        assertEquals("login", result.getName());
    }

    @Test
    void createUser_WithEmptyEmail_ShouldThrowValidationException() {
        User user = createValidUser();
        user.setEmail("");

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void createUser_WithInvalidEmail_ShouldThrowValidationException() {
        User user = createValidUser();
        user.setEmail("invalid-email");

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void createUser_WithEmptyLogin_ShouldThrowValidationException() {
        User user = createValidUser();
        user.setLogin("");

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void createUser_WithLoginContainingSpaces_ShouldThrowValidationException() {
        User user = createValidUser();
        user.setLogin("login with spaces");

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void createUser_WithFutureBirthday_ShouldThrowValidationException() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void updateUser_WithNonExistentId_ShouldThrowValidationException() {
        User user = createValidUser();
        user.setId(999L);

        assertThrows(ValidationException.class, () -> userController.update(user));
    }

    @Test
    void updateUser_WithNullId_ShouldThrowValidationException() {
        User user = createValidUser();
        user.setId(null);

        assertThrows(ValidationException.class, () -> userController.update(user));
    }

    @Test
    void findAll_WhenNoUsers_ShouldReturnEmptyCollection() {
        assertTrue(userController.findAll().isEmpty());
    }

    @Test
    void findAll_WhenUsersExist_ShouldReturnAllUsers() {
        User user1 = userController.create(createValidUser());
        User user2 = userController.create(createValidUser());
        user2.setEmail("another@example.com");

        var result = userController.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUserSuccessfully() {
        User originalUser = userController.create(createValidUser());
        User updatedUser = createValidUser();
        updatedUser.setId(originalUser.getId());
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@example.com");

        User result = userController.update(updatedUser);

        assertEquals("Updated Name", result.getName());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals(originalUser.getId(), result.getId());
    }

    @Test
    void createUser_WithNullUser_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> userController.create(null));
    }

    @Test
    void createUser_WithUserHavingNullFields_ShouldThrowValidationException() {
        User user = new User();

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}