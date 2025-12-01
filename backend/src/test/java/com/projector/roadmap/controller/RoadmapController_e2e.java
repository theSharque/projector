package com.projector.roadmap.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import com.projector.TestFunctions;
import com.projector.roadmap.model.Roadmap;

/**
 * E2E тесты для RoadmapController.
 * Использует testcontainers для изолированной PostgreSQL БД и webClient для HTTP запросов.
 * Тестирует controller, service, repository без моков.
 */
@DirtiesContext
public class RoadmapController_e2e extends TestFunctions {

    private String authToken;

    @BeforeEach
    public void setUp() {
        initWebTestClient();
        // Логинимся как admin для получения токена
        authToken = loginAndGetToken("admin", "admin");
    }

    @Test
    @Order(1)
    public void testGetAllRoadmaps_Success() {
        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/roadmaps")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Roadmap.class)
                .value(roadmaps -> {
                    assert roadmaps != null;
                });
    }

    @Test
    @Order(2)
    public void testCreateRoadmap_Success() {
        // Given
        Roadmap newRoadmap = createTestRoadmap(null, "Test Project", 1L,
                "Build amazing software", "Test roadmap description");

        // When & Then
        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/roadmaps")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newRoadmap)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Roadmap.class)
                .value(roadmap -> {
                    assert roadmap != null;
                    assert roadmap.getId() != null;
                    assert roadmap.getProjectName().equals("Test Project");
                    assert roadmap.getAuthorId().equals(1L);
                    assert roadmap.getMission().equals("Build amazing software");
                });
    }

    @Test
    @Order(3)
    public void testGetRoadmapById_Success() {
        // Given - создаем roadmap
        Roadmap roadmapToCreate = createTestRoadmap(null, "Project to Get", 1L,
                "Mission", "Description");
        Roadmap createdRoadmap = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/roadmaps")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roadmapToCreate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Roadmap.class)
                .returnResult()
                .getResponseBody();

        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/roadmaps/" + createdRoadmap.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Roadmap.class)
                .value(roadmap -> {
                    assert roadmap != null;
                    assert roadmap.getId().equals(createdRoadmap.getId());
                    assert roadmap.getProjectName().equals("Project to Get");
                });
    }

    @Test
    @Order(4)
    public void testGetRoadmapById_NotFound() {
        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/roadmaps/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(5)
    public void testUpdateRoadmap_Success() {
        // Given - создаем roadmap для обновления
        Roadmap roadmapToCreate = createTestRoadmap(null, "Project to Update", 1L,
                "Old Mission", "Old Description");
        Roadmap createdRoadmap = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/roadmaps")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roadmapToCreate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Roadmap.class)
                .returnResult()
                .getResponseBody();

        // Обновляем roadmap
        Roadmap updatedRoadmap = createTestRoadmap(createdRoadmap.getId(), "Updated Project",
                1L, "New Mission", "New Description");

        // When & Then
        webTestClientWithAuth(authToken)
                .put()
                .uri("/api/roadmaps/" + createdRoadmap.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedRoadmap)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Roadmap.class)
                .value(roadmap -> {
                    assert roadmap != null;
                    assert roadmap.getProjectName().equals("Updated Project");
                    assert roadmap.getMission().equals("New Mission");
                });
    }

    @Test
    @Order(6)
    public void testUpdateRoadmap_NotFound() {
        // Given
        Roadmap roadmap = createTestRoadmap(999L, "Not Found", 1L, "Mission", "Description");

        // When & Then
        webTestClientWithAuth(authToken)
                .put()
                .uri("/api/roadmaps/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roadmap)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(7)
    public void testDeleteRoadmap_Success() {
        // Given - создаем roadmap для удаления
        Roadmap roadmapToCreate = createTestRoadmap(null, "Project to Delete", 1L,
                "Mission", "Description");
        Roadmap createdRoadmap = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/roadmaps")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roadmapToCreate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Roadmap.class)
                .returnResult()
                .getResponseBody();

        // When & Then
        webTestClientWithAuth(authToken)
                .delete()
                .uri("/api/roadmaps/" + createdRoadmap.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Проверяем, что roadmap действительно удален
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/roadmaps/" + createdRoadmap.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(8)
    public void testDeleteRoadmap_NotFound() {
        // When & Then
        webTestClientWithAuth(authToken)
                .delete()
                .uri("/api/roadmaps/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(9)
    public void testUnauthorizedAccess() {
        // When & Then - без токена должен вернуть 401
        webTestClient
                .get()
                .uri("/api/roadmaps")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}

