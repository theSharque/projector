package com.projector.feature.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import com.projector.TestFunctions;
import com.projector.feature.model.Feature;
import com.projector.feature.model.Quarter;

/**
 * E2E тесты для FeatureController.
 * Использует testcontainers для изолированной PostgreSQL БД и webClient для HTTP запросов.
 * Тестирует controller, service, repository без моков.
 */
@DirtiesContext
public class FeatureController_e2e extends TestFunctions {

    private String authToken;

    @BeforeEach
    public void setUp() {
        initWebTestClient();
        // Логинимся как admin для получения токена
        authToken = loginAndGetToken("admin", "admin");
    }

    @Test
    @Order(1)
    public void testGetAllFeatures_Success() {
        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/features")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Feature.class)
                .value(features -> {
                    assert features != null;
                });
    }

    @Test
    @Order(2)
    public void testCreateFeature_Success() {
        // Given
        Feature newFeature = createTestFeature(null, 2024L, Quarter.Q1, 1L,
                "User authentication feature", "Implement user login and registration");

        // When & Then
        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/features")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newFeature)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Feature.class)
                .value(feature -> {
                    assert feature != null;
                    assert feature.getId() != null;
                    assert feature.getYear().equals(2024L);
                    assert feature.getQuarter().equals(Quarter.Q1);
                    assert feature.getAuthorId().equals(1L);
                    assert feature.getSummary().equals("User authentication feature");
                });
    }

    @Test
    @Order(3)
    public void testGetFeatureById_Success() {
        // Given - создаем feature
        Feature featureToCreate = createTestFeature(null, 2024L, Quarter.Q2, 1L,
                "Feature to Get", "Description");
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

        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/features/" + createdFeature.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Feature.class)
                .value(feature -> {
                    assert feature != null;
                    assert feature.getId().equals(createdFeature.getId());
                    assert feature.getSummary().equals("Feature to Get");
                });
    }

    @Test
    @Order(4)
    public void testGetFeatureById_NotFound() {
        // When & Then
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/features/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(5)
    public void testUpdateFeature_Success() {
        // Given - создаем feature для обновления
        Feature featureToCreate = createTestFeature(null, 2024L, Quarter.Q3, 1L,
                "Feature to Update", "Old Description");
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

        // Обновляем feature
        Feature updatedFeature = createTestFeature(createdFeature.getId(), 2024L, Quarter.Q4,
                1L, "Updated Feature", "New Description");

        // When & Then
        webTestClientWithAuth(authToken)
                .put()
                .uri("/api/features/" + createdFeature.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedFeature)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Feature.class)
                .value(feature -> {
                    assert feature != null;
                    assert feature.getSummary().equals("Updated Feature");
                    assert feature.getQuarter().equals(Quarter.Q4);
                });
    }

    @Test
    @Order(6)
    public void testUpdateFeature_NotFound() {
        // Given
        Feature feature = createTestFeature(999L, 2024L, Quarter.Q1, 1L, "Not Found", "Description");

        // When & Then
        webTestClientWithAuth(authToken)
                .put()
                .uri("/api/features/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(feature)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(7)
    public void testDeleteFeature_Success() {
        // Given - создаем feature для удаления
        Feature featureToCreate = createTestFeature(null, 2024L, Quarter.Q1, 1L,
                "Feature to Delete", "Description");
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

        // When & Then
        webTestClientWithAuth(authToken)
                .delete()
                .uri("/api/features/" + createdFeature.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Проверяем, что feature действительно удален
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/features/" + createdFeature.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(8)
    public void testDeleteFeature_NotFound() {
        // When & Then
        webTestClientWithAuth(authToken)
                .delete()
                .uri("/api/features/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(9)
    public void testUnauthorizedAccess() {
        // When & Then - без токена должен вернуть 401
        webTestClient
                .get()
                .uri("/api/features")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @Order(10)
    public void testCreateFeatureWithoutFunctionalAreas() {
        // Given - feature without functional area IDs
        Feature newFeature = createTestFeature(null, 2024L, Quarter.Q1, 1L,
                "Feature without FA", "Should fail validation");
        newFeature.setFunctionalAreaIds(java.util.List.of()); // Explicitly set empty list

        // When & Then - should return 400 because functional area IDs are required
        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/features")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newFeature)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(11)
    public void testCreateFeatureWithInvalidFunctionalAreaId() {
        // Given - feature with non-existent functional area ID
        Feature newFeature = createTestFeature(null, 2024L, Quarter.Q1, 1L,
                "Feature with invalid FA", "Should fail validation");
        newFeature.setFunctionalAreaIds(java.util.List.of(9999L));

        // When & Then - should return 400 because FA ID doesn't exist
        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/features")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newFeature)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(12)
    public void testCreateFeatureWithValidFunctionalAreas() {
        // Given - create a functional area first
        com.projector.functionalarea.model.FunctionalArea fa = com.projector.functionalarea.model.FunctionalArea.builder()
                .name("Test FA for Feature")
                .description("Test functional area")
                .build();

        com.projector.functionalarea.model.FunctionalArea createdFa = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/functional-areas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fa)
                .exchange()
                .expectStatus().isOk()
                .expectBody(com.projector.functionalarea.model.FunctionalArea.class)
                .returnResult()
                .getResponseBody();

        // Create feature with valid functional area
        Feature newFeature = createTestFeature(null, 2024L, Quarter.Q1, 1L,
                "Feature with valid FA", "Should succeed");
        newFeature.setFunctionalAreaIds(java.util.List.of(createdFa.getId()));

        // When & Then - should succeed
        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/features")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newFeature)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Feature.class)
                .value(feature -> {
                    assert feature != null;
                    assert feature.getId() != null;
                    assert feature.getFunctionalAreaIds().contains(createdFa.getId());
                });
    }
}

