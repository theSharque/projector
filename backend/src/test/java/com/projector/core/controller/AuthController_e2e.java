package com.projector.core.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import com.projector.TestFunctions;
import com.projector.core.model.UserCredentials;

/**
 * E2E тесты для AuthController.
 * Использует testcontainers для изолированной PostgreSQL БД и webClient для HTTP запросов.
 * Тестирует controller, service, repository без моков.
 */
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthController_e2e extends TestFunctions {

    @BeforeEach
    public void setUp() {
        initWebTestClient();
    }

    @Test
    @Order(1)
    public void testLogin_Success() {
        // Given - пользователь admin уже создан в миграции БД
        UserCredentials credentials = new UserCredentials("admin", "admin");

        // When & Then
        webTestClient
                .post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(credentials)
                .exchange()
                .expectStatus().isNoContent()
                .expectHeader().exists("Set-Cookie")
                .expectHeader().value("Set-Cookie", value -> {
                    assert value != null;
                    assert value.contains("X-Auth=");
                    assert value.contains("Path=/");
                })
                .expectBody().isEmpty();
    }

    @Test
    @Order(2)
    public void testLogin_InvalidPassword() {
        // Given
        UserCredentials credentials = new UserCredentials("admin", "wrongpassword");

        // When & Then
        webTestClient
                .post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(credentials)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @Order(3)
    public void testLogin_UserNotFound() {
        // Given
        UserCredentials credentials = new UserCredentials("nonexistent@example.com", "password");

        // When & Then
        webTestClient
                .post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(credentials)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @Order(4)
    public void testLogin_InvalidJson() {
        // When & Then
        webTestClient
                .post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{ invalid json }")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(5)
    public void testGetProfile_Unauthenticated() {
        // When & Then - без токена должен вернуть 401
        webTestClient
                .get()
                .uri("/api/auth/profile")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @Order(6)
    public void testGetProfile_Authenticated() {
        // Given - логинимся и получаем токен
        String token = loginAndGetToken("admin", "admin");

        // When & Then
        webTestClientWithAuth(token)
                .get()
                .uri("/api/auth/profile")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(String.class)
                .value(authorities -> {
                    assert authorities != null;
                    assert !authorities.isEmpty();
                });
    }
}

