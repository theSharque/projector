package com.projector.task.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import com.projector.TestFunctions;
import com.projector.feature.model.Feature;
import com.projector.feature.model.Quarter;
import com.projector.functionalarea.model.FunctionalArea;
import com.projector.roadmap.model.Roadmap;
import com.projector.task.model.Task;

/**
 * E2E тесты для TaskController.
 * Использует testcontainers для изолированной PostgreSQL БД и webClient для HTTP запросов.
 * Тестирует controller, service, repository без моков.
 */
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskController_e2e extends TestFunctions {

    private String authToken;
    private Long featureId;
    private Long roadmapId;

    @BeforeEach
    public void setUp() {
        initWebTestClient();
        // Логинимся как admin для получения токена
        authToken = loginAndGetToken("admin", "admin");

        // Создаем roadmap для feature и tasks
        Roadmap roadmapToCreate = createTestRoadmap(null, "Test Roadmap for Tasks", 1L,
                "Test roadmap mission", "Test roadmap description");
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
        roadmapId = createdRoadmap.getId();

        // Создаем FA для feature (если еще не существует)
        FunctionalArea fa = FunctionalArea.builder()
                .name("TaskTest_FA_" + System.currentTimeMillis()) // Unique name
                .description("FA for task tests")
                .build();
        FunctionalArea createdFa = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/functional-areas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fa)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FunctionalArea.class)
                .returnResult()
                .getResponseBody();

        // Создаем feature для тестов задач (задачи зависят от feature)
        Feature featureToCreate = createTestFeatureWithFa(null, 2024L, Quarter.Q1, 1L,
                "Test Feature for Tasks", "Description", java.util.List.of(createdFa.getId()));
        Feature createdFeature = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/features")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(featureToCreate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Feature.class)
                .returnResult()
                .getResponseBody();
        featureId = createdFeature.getId();
    }

    @Test
    @Order(1)
    public void testGetAllTasks_Success() {
        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Task.class)
                .value(tasks -> {
                    assert tasks != null;
                });
    }

    @Test
    @Order(2)
    public void testCreateTask_Success() {
        // Given
        Task newTask = createTestTask(null, featureId, roadmapId, 1L,
                "Implement login endpoint", "Create REST API endpoint for user login");

        // When & Then
        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newTask)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Task.class)
                .value(task -> {
                    assert task != null;
                    assert task.getId() != null;
                    assert task.getFeatureId().equals(featureId);
                    assert task.getAuthorId().equals(1L);
                    assert task.getSummary().equals("Implement login endpoint");
                });
    }

    @Test
    @Order(3)
    public void testGetTaskById_Success() {
        // Given - создаем task
        Task taskToCreate = createTestTask(null, featureId, roadmapId, 1L,
                "Task to Get", "Description");
        Task createdTask = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(taskToCreate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Task.class)
                .returnResult()
                .getResponseBody();

        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/tasks/" + createdTask.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Task.class)
                .value(task -> {
                    assert task != null;
                    assert task.getId().equals(createdTask.getId());
                    assert task.getSummary().equals("Task to Get");
                });
    }

    @Test
    @Order(4)
    public void testGetTaskById_NotFound() {
        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/tasks/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(5)
    public void testUpdateTask_Success() {
        // Given - создаем task для обновления
        Task taskToCreate = createTestTask(null, featureId, roadmapId, 1L,
                "Task to Update", "Old Description");
        Task createdTask = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(taskToCreate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Task.class)
                .returnResult()
                .getResponseBody();

        // Обновляем task
        Task updatedTask = createTestTask(createdTask.getId(), featureId, roadmapId, 1L,
                "Updated Task", "New Description");

        // When & Then
        webTestClientWithAuth(authToken)
                .put()
                .uri("/api/tasks/" + createdTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedTask)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Task.class)
                .value(task -> {
                    assert task != null;
                    assert task.getSummary().equals("Updated Task");
                    assert task.getDescription().equals("New Description");
                });
    }

    @Test
    @Order(6)
    public void testUpdateTask_NotFound() {
        // Given
        Task task = createTestTask(999L, featureId, roadmapId, 1L, "Not Found", "Description");

        // When & Then
        webTestClientWithAuth(authToken)
                .put()
                .uri("/api/tasks/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(task)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(7)
    public void testDeleteTask_Success() {
        // Given - создаем task для удаления
        Task taskToCreate = createTestTask(null, featureId, roadmapId, 1L,
                "Task to Delete", "Description");
        Task createdTask = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(taskToCreate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Task.class)
                .returnResult()
                .getResponseBody();

        // When & Then
        webTestClientWithAuth(authToken)
                .delete()
                .uri("/api/tasks/" + createdTask.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Проверяем, что task действительно удален
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/tasks/" + createdTask.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(8)
    public void testDeleteTask_NotFound() {
        // When & Then
        webTestClientWithAuth(authToken)
                .delete()
                .uri("/api/tasks/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(9)
    public void testUnauthorizedAccess() {
        // When & Then - без токена должен вернуть 401
        webTestClient
                .get()
                .uri("/api/tasks")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}

