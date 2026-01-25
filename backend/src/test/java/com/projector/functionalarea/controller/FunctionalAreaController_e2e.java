package com.projector.functionalarea.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.projector.TestFunctions;
import com.projector.functionalarea.model.FunctionalArea;

public class FunctionalAreaController_e2e extends TestFunctions {

    private String authToken;

    @BeforeEach
    void setup() {
        initWebTestClient();
        authToken = loginAndGetToken("admin", "admin");
    }

    @Test
    void testGetAllFunctionalAreas() {
        // Create a functional area first
        FunctionalArea fa = FunctionalArea.builder()
                .name("User Management")
                .description("All features related to user management")
                .build();

        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/functional-areas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fa)
                .exchange()
                .expectStatus().isOk();

        // Get all functional areas
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/functional-areas")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FunctionalArea.class)
                .hasSize(1);
    }

    @Test
    void testGetFunctionalAreaById() {
        // Create a functional area
        FunctionalArea fa = FunctionalArea.builder()
                .name("Data Management")
                .description("Database and data handling")
                .build();

        FunctionalArea created = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/functional-areas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fa)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FunctionalArea.class)
                .returnResult()
                .getResponseBody();

        // Get by ID
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/functional-areas/" + created.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(FunctionalArea.class)
                .isEqualTo(created);
    }

    @Test
    void testCreateFunctionalArea() {
        FunctionalArea fa = FunctionalArea.builder()
                .name("Authentication")
                .description("Login and security features")
                .build();

        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/functional-areas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fa)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FunctionalArea.class)
                .value(created -> {
                    assert created.getId() != null;
                    assert created.getName().equals("Authentication");
                    assert created.getCreateDate() != null;
                });
    }

    @Test
    void testUpdateFunctionalArea() {
        // Create a functional area
        FunctionalArea fa = FunctionalArea.builder()
                .name("Original Name")
                .description("Original description")
                .build();

        FunctionalArea created = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/functional-areas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fa)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FunctionalArea.class)
                .returnResult()
                .getResponseBody();

        // Update it
        FunctionalArea updated = FunctionalArea.builder()
                .name("Updated Name")
                .description("Updated description")
                .build();

        webTestClientWithAuth(authToken)
                .put()
                .uri("/api/functional-areas/" + created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updated)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FunctionalArea.class)
                .value(result -> {
                    assert result.getName().equals("Updated Name");
                    assert result.getDescription().equals("Updated description");
                });
    }

    @Test
    void testDeleteFunctionalAreaWithReplacement() {
        // Create two functional areas
        FunctionalArea fa1 = FunctionalArea.builder()
                .name("FA to Delete")
                .description("This will be deleted")
                .build();

        FunctionalArea created1 = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/functional-areas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fa1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FunctionalArea.class)
                .returnResult()
                .getResponseBody();

        FunctionalArea fa2 = FunctionalArea.builder()
                .name("Replacement FA")
                .description("This will replace the deleted one")
                .build();

        FunctionalArea created2 = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/functional-areas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fa2)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FunctionalArea.class)
                .returnResult()
                .getResponseBody();

        // Create a feature using FA1
        com.projector.feature.model.Feature feature = com.projector.feature.model.Feature.builder()
                .year(2024L)
                .quarter(com.projector.feature.model.Quarter.Q1)
                .authorId(1L)
                .sprint(1L)
                .release("v1.0.0")
                .summary("Test Feature")
                .description("Test description")
                .functionalAreaIds(List.of(created1.getId()))
                .build();

        com.projector.feature.model.Feature createdFeature = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/features")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(feature)
                .exchange()
                .expectStatus().isOk()
                .expectBody(com.projector.feature.model.Feature.class)
                .returnResult()
                .getResponseBody();

        // Delete FA1 with replacement FA2
        webTestClientWithAuth(authToken)
                .delete()
                .uri("/api/functional-areas/" + created1.getId() + "?replacementFaId=" + created2.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Verify FA1 is deleted
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/functional-areas/" + created1.getId())
                .exchange()
                .expectStatus().isNotFound();

        // Verify feature now has FA2
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/features/" + createdFeature.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(com.projector.feature.model.Feature.class)
                .value(result -> {
                    assert result.getFunctionalAreaIds().contains(created2.getId());
                    assert !result.getFunctionalAreaIds().contains(created1.getId());
                });
    }

    @Test
    void testDeleteFunctionalAreaWithoutReplacement() {
        // Create a functional area
        FunctionalArea fa = FunctionalArea.builder()
                .name("FA without replacement")
                .description("This deletion should fail")
                .build();

        FunctionalArea created = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/functional-areas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fa)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FunctionalArea.class)
                .returnResult()
                .getResponseBody();

        // Try to delete without replacementFaId - should fail with 400
        webTestClientWithAuth(authToken)
                .delete()
                .uri("/api/functional-areas/" + created.getId())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testGetFunctionalAreaUsage() {
        // Create a functional area
        FunctionalArea fa = FunctionalArea.builder()
                .name("FA with Usage")
                .description("Will be used by features")
                .build();

        FunctionalArea created = webTestClientWithAuth(authToken)
                .post()
                .uri("/api/functional-areas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fa)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FunctionalArea.class)
                .returnResult()
                .getResponseBody();

        // Check usage (should be 0)
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/functional-areas/" + created.getId() + "/usage")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .isEqualTo(0L);

        // Create a feature using this FA
        com.projector.feature.model.Feature feature = com.projector.feature.model.Feature.builder()
                .year(2024L)
                .quarter(com.projector.feature.model.Quarter.Q1)
                .authorId(1L)
                .sprint(1L)
                .release("v1.0.0")
                .summary("Test Feature")
                .description("Test description")
                .functionalAreaIds(List.of(created.getId()))
                .build();

        webTestClientWithAuth(authToken)
                .post()
                .uri("/api/features")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(feature)
                .exchange()
                .expectStatus().isOk();

        // Check usage again (should be 1)
        webTestClientWithAuth(authToken)
                .get()
                .uri("/api/functional-areas/" + created.getId() + "/usage")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .isEqualTo(1L);
    }
}
