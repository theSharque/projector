package com.projector.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import com.projector.TestFunctions;
import com.projector.user.model.User;

/**
 * E2E тесты для UserController.
 * Использует testcontainers для изолированной PostgreSQL БД и webClient для
 * HTTP запросов.
 * Тестирует controller, service, repository без моков.
 */
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserController_e2e extends TestFunctions {

    private String authToken;

    @BeforeEach
    public void setUp() {
        initWebTestClient();
        // Логинимся как admin для получения токена
        authToken = loginAndGetToken("admin", "admin");
    }

    @Test
    @Order(1)
    public void testGetAllUsers_Success() {
        // Given - пользователь admin уже создан в миграции БД

        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(User.class)
                .value(users -> {
                    assert users != null;
                    assert !users.isEmpty();
                    // Проверяем, что есть пользователь admin
                    assert users.stream().anyMatch(u -> u.getEmail().equals("admin"));
                });
    }

    @Test
    @Order(2)
    public void testGetUserById_Success() {
        // Given - пользователь с id=1 (admin) уже существует в миграции

        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(User.class)
                .value(user -> {
                    assert user != null;
                    assert user.getId().equals(1L);
                    assert user.getEmail().equals("admin");
                    assert user.getPassword() == null; // Password не должен возвращаться
                    assert user.getPassHash() == null; // PassHash не должен возвращаться
                });
    }

    @Test
    @Order(3)
    public void testGetUserById_NotFound() {
        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/users/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(4)
    public void testGetUserByEmail_Success() {
        // Given - пользователь admin уже существует

        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/users/email/admin")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(User.class)
                .value(user -> {
                    assert user != null;
                    assert user.getEmail().equals("admin");
                    assert user.getPassword() == null;
                });
    }

    @Test
    @Order(5)
    public void testCreateUser_Success() {
        // Given
        String jsonBody = """
                {
                    "email": "testuser@example.com",
                    "password": "testpass123"
                }
                """;

        // When & Then
        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(User.class)
                .value(user -> {
                    assert user != null;
                    assert user.getId() != null;
                    assert user.getEmail().equals("testuser@example.com");
                    assert user.getPassword() == null; // Password не должен возвращаться
                    assert user.getPassHash() == null; // PassHash не должен возвращаться
                });
    }

    @Test
    @Order(6)
    public void testCreateUser_DuplicateEmail() {
        // Given - пытаемся создать пользователя с email, который уже существует
        User duplicateUser = createTestUser(null, "admin", "password");

        // When & Then
        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(duplicateUser)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(7)
    public void testCreateUser_InvalidEmail() {
        // Given - пользователь с невалидным email
        User invalidUser = createTestUser(null, "invalid-email", "password");

        // When & Then
        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidUser)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(8)
    public void testCreateUser_WithRoles() {
        // Given - создаем пользователя с ролями
        String jsonBody = """
                {
                    "email": "userwithroles@example.com",
                    "password": "password123",
                    "roleIds": [1]
                }
                """;

        // When & Then
        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(User.class)
                .value(user -> {
                    assert user != null;
                    assert user.getId() != null;
                    assert user.getEmail().equals("userwithroles@example.com");
                });
    }

    @Test
    @Order(9)
    public void testUpdateUser_Success() {
        // Given - создаем пользователя для обновления
        String createJson = """
                {
                    "email": "usertoupdate@example.com",
                    "password": "password123"
                }
                """;
        User createdUser = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createJson)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .returnResult()
                .getResponseBody();

        // Обновляем пользователя
        String updateJson = String.format("""
                {
                    "id": %d,
                    "email": "updated@example.com",
                    "password": "newpassword123",
                    "roleIds": [1]
                }
                """, createdUser.getId());

        // When & Then
        webTestClientWithAuth(authToken)
                .put()
                .uri("/api/users/" + createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateJson)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(User.class)
                .value(user -> {
                    assert user != null;
                    assert user.getEmail().equals("updated@example.com");
                });
    }

    @Test
    @Order(10)
    public void testUpdateUser_NotFound() {
        // Given
        User user = createTestUser(999L, "notfound@example.com", "password");

        // When & Then
        webTestClientWithAuth(authToken)
                .put()
                .uri("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(11)
    public void testUpdateUser_WithoutPassword() {
        // Given - создаем пользователя
        String createJson = """
                {
                    "email": "userwithoutpass@example.com",
                    "password": "password123"
                }
                """;
        User createdUser = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createJson)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .returnResult()
                .getResponseBody();

        // Обновляем без пароля (пароль должен остаться прежним)
        String updateJson = String.format("""
                {
                    "id": %d,
                    "email": "updatedwithoutpass@example.com"
                }
                """, createdUser.getId());

        // When & Then
        webTestClientWithAuth(authToken)
                .put()
                .uri("/api/users/" + createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateJson)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(User.class)
                .value(user -> {
                    assert user != null;
                    assert user.getEmail().equals("updatedwithoutpass@example.com");
                });
    }

    @Test
    @Order(12)
    public void testDeleteUser_Success() {
        // Given - создаем пользователя для удаления
        String createJson = """
                {
                    "email": "usertodelete@example.com",
                    "password": "password123"
                }
                """;
        User createdUser = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createJson)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .returnResult()
                .getResponseBody();

        // When & Then
        webTestClientWithAuth(authToken)
                .delete()
                .uri("/api/users/" + createdUser.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Проверяем, что пользователь действительно удален
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/users/" + createdUser.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(13)
    public void testDeleteUser_NotFound() {
        // When & Then
        webTestClientWithAuth(authToken)
                .delete()
                .uri("/api/users/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(14)
    public void testUnauthorizedAccess() {
        // When & Then - без токена должен вернуть 401
        webTestClient
                .get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
