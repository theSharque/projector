package com.projector.role.controller;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import com.projector.TestFunctions;
import com.projector.role.model.Role;

/**
 * E2E тесты для RoleController.
 * Использует testcontainers для изолированной PostgreSQL БД и webClient для HTTP запросов.
 * Тестирует controller, service, repository без моков.
 */
@DirtiesContext
public class RoleController_e2e extends TestFunctions {

    private String authToken;

    @BeforeEach
    public void setUp() {
        initWebTestClient();
        // Логинимся как admin для получения токена
        authToken = loginAndGetToken("admin", "admin");
    }

    @Test
    @Order(1)
    public void testGetAllRoles_Success() {
        // Given - роль SUPERADMIN уже создана в миграции БД

        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/roles")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Role.class)
                .value(roles -> {
                    assert roles != null;
                    assert !roles.isEmpty();
                    // Проверяем, что есть роль SUPERADMIN
                    assert roles.stream().anyMatch(r -> r.getName().equals("SUPERADMIN"));
                });
    }

    @Test
    @Order(2)
    public void testGetRoleById_Success() {
        // Given - роль с id=1 (SUPERADMIN) уже существует в миграции

        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/roles/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Role.class)
                .value(role -> {
                    assert role != null;
                    assert role.getId().equals(1L);
                    assert role.getName().equals("SUPERADMIN");
                    assert role.getAuthorities() != null;
                    assert !role.getAuthorities().isEmpty();
                });
    }

    @Test
    @Order(3)
    public void testGetRoleById_NotFound() {
        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/roles/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(4)
    public void testCreateRole_Success() {
        // Given
        Role newRole = createTestRole(null, "TEST_ROLE", Set.of("USER_VIEW", "ROLE_VIEW"));

        // When & Then
        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newRole)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Role.class)
                .value(role -> {
                    assert role != null;
                    assert role.getId() != null;
                    assert role.getName().equals("TEST_ROLE");
                    assert role.getAuthorities().contains("USER_VIEW");
                    assert role.getAuthorities().contains("ROLE_VIEW");
                });
    }

    @Test
    @Order(5)
    public void testCreateRole_DuplicateName() {
        // Given - пытаемся создать роль с именем, которое уже существует
        Role duplicateRole = createTestRole(null, "SUPERADMIN", Set.of("USER_VIEW"));

        // When & Then
        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(duplicateRole)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(6)
    public void testCreateRole_InvalidInput() {
        // Given - роль с пустым именем
        Role invalidRole = new Role();
        invalidRole.setName("");

        // When & Then
        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRole)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(7)
    public void testUpdateRole_Success() {
        // Given - создаем роль для обновления
        Role roleToCreate = createTestRole(null, "ROLE_TO_UPDATE", Set.of("USER_VIEW"));
        Role createdRole = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roleToCreate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Role.class)
                .returnResult()
                .getResponseBody();

        // Обновляем роль
        Role updatedRole = createTestRole(createdRole.getId(), "UPDATED_ROLE",
                Set.of("USER_VIEW", "ROLE_VIEW", "ROADMAP_VIEW"));

        // When & Then
        webTestClientWithAuth(authToken)
                .put()
                .uri("/api/roles/" + createdRole.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedRole)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Role.class)
                .value(role -> {
                    assert role != null;
                    assert role.getName().equals("UPDATED_ROLE");
                    assert role.getAuthorities().contains("USER_VIEW");
                    assert role.getAuthorities().contains("ROLE_VIEW");
                    assert role.getAuthorities().contains("ROADMAP_VIEW");
                });
    }

    @Test
    @Order(8)
    public void testUpdateRole_NotFound() {
        // Given
        Role role = createTestRole(999L, "NOT_FOUND", Set.of("USER_VIEW"));

        // When & Then
        webTestClientWithAuth(authToken)
                .put()
                .uri("/api/roles/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(role)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(9)
    public void testUpdateAuthorities_Success() {
        // Given - создаем роль для обновления authorities
        Role roleToCreate = createTestRole(null, "ROLE_FOR_AUTHORITIES", Set.of("USER_VIEW"));
        Role createdRole = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roleToCreate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Role.class)
                .returnResult()
                .getResponseBody();

        // Обновляем authorities
        Set<String> newAuthorities = Set.of("USER_VIEW", "ROLE_VIEW", "ROADMAP_VIEW", "FEATURE_VIEW");

        // When & Then
        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/roles/" + createdRole.getId() + "/authorities")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newAuthorities)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Role.class)
                .value(role -> {
                    assert role != null;
                    assert role.getAuthorities().size() == 4;
                    assert role.getAuthorities().containsAll(newAuthorities);
                });
    }

    @Test
    @Order(10)
    public void testDeleteRole_Success() {
        // Given - создаем роль для удаления
        Role roleToCreate = createTestRole(null, "ROLE_TO_DELETE", Set.of("USER_VIEW"));
        Role createdRole = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roleToCreate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Role.class)
                .returnResult()
                .getResponseBody();

        // When & Then
        webTestClientWithAuth(authToken)
                .delete()
                .uri("/api/roles/" + createdRole.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Проверяем, что роль действительно удалена
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/roles/" + createdRole.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(11)
    public void testDeleteRole_NotFound() {
        // When & Then
        webTestClientWithAuth(authToken)
                .delete()
                .uri("/api/roles/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(12)
    public void testUnauthorizedAccess() {
        // When & Then - без токена должен вернуть 401
        webTestClient
                .get()
                .uri("/api/roles")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}

