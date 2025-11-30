package com.projector.feature.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebInputException;

import com.projector.feature.model.Feature;
import com.projector.feature.model.Quarter;
import com.projector.feature.service.FeatureService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class FeatureControllerTest {

    @Mock
    private FeatureService featureService;

    @InjectMocks
    private FeatureController featureController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllFeatures_Success() {
        Feature feature1 = createFeature(1L, 2024L, Quarter.Q1, null, null, "Feature 1", "Description 1", 1L);
        Feature feature2 = createFeature(2L, 2024L, Quarter.Q2, 1L, "v1.0", "Feature 2", "Description 2", 1L);

        when(featureService.getAllFeatures()).thenReturn(Flux.just(feature1, feature2));

        StepVerifier.create(featureController.getAllFeatures())
                .expectNext(feature1)
                .expectNext(feature2)
                .verifyComplete();

        verify(featureService, times(1)).getAllFeatures();
    }

    @Test
    public void testGetFeatureById_Success() {
        Feature feature = createFeature(1L, 2024L, Quarter.Q1, null, null, "Test Feature", "Test Description", 1L);

        when(featureService.getFeatureById(1L)).thenReturn(Mono.just(feature));

        StepVerifier.create(featureController.getFeatureById(1L))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getId().equals(1L)
                                && response.getBody().getYear().equals(2024L)
                                && response.getBody().getQuarter() == Quarter.Q1)
                .verifyComplete();

        verify(featureService, times(1)).getFeatureById(1L);
    }

    @Test
    public void testGetFeatureById_NotFound() {
        when(featureService.getFeatureById(1L))
                .thenReturn(Mono.error(new ServerWebInputException("Feature not found")));

        StepVerifier.create(featureController.getFeatureById(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(featureService, times(1)).getFeatureById(1L);
    }

    @Test
    public void testCreateFeature_Success() {
        Feature feature = createFeature(null, 2024L, Quarter.Q1, 1L, "v1.0", "New Feature", "New Description", 1L);
        Feature savedFeature = createFeature(1L, 2024L, Quarter.Q1, 1L, "v1.0", "New Feature", "New Description", 1L);
        savedFeature.setCreateDate(LocalDateTime.now());
        savedFeature.setUpdateDate(LocalDateTime.now());

        when(featureService.createFeature(any(Feature.class))).thenReturn(Mono.just(savedFeature));

        StepVerifier.create(featureController.createFeature(feature))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getId().equals(1L)
                                && response.getBody().getCreateDate() != null
                                && response.getBody().getUpdateDate() != null
                                && response.getBody().equals(savedFeature))
                .verifyComplete();

        verify(featureService, times(1)).createFeature(any(Feature.class));
    }

    @Test
    public void testCreateFeature_Error() {
        Feature feature = createFeature(null, 2024L, Quarter.Q1, null, null, "Feature", "Description", 1L);

        when(featureService.createFeature(any(Feature.class)))
                .thenReturn(Mono.error(new ServerWebInputException("Invalid input")));

        StepVerifier.create(featureController.createFeature(feature))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(featureService, times(1)).createFeature(any(Feature.class));
    }

    @Test
    public void testUpdateFeature_Success() {
        Feature feature = createFeature(1L, 2024L, Quarter.Q2, 2L, "v1.1", "Updated Feature", "Updated Description",
                1L);
        feature.setCreateDate(LocalDateTime.now().minusDays(5));
        feature.setUpdateDate(LocalDateTime.now());

        when(featureService.updateFeature(anyLong(), any(Feature.class))).thenReturn(Mono.just(feature));

        StepVerifier.create(featureController.updateFeature(1L, feature))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getId().equals(1L)
                                && response.getBody().getQuarter() == Quarter.Q2)
                .verifyComplete();

        verify(featureService, times(1)).updateFeature(eq(1L), any(Feature.class));
    }

    @Test
    public void testUpdateFeature_NotFound() {
        Feature feature = createFeature(1L, 2024L, Quarter.Q1, null, null, "Feature", "Description", 1L);

        when(featureService.updateFeature(anyLong(), any(Feature.class)))
                .thenReturn(Mono.error(new ServerWebInputException("Feature not found")));

        StepVerifier.create(featureController.updateFeature(1L, feature))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(featureService, times(1)).updateFeature(eq(1L), any(Feature.class));
    }

    @Test
    public void testUpdateFeature_Error() {
        Feature feature = createFeature(1L, 2024L, Quarter.Q1, null, null, "Feature", "Description", 1L);

        when(featureService.updateFeature(anyLong(), any(Feature.class)))
                .thenReturn(Mono.error(new ServerWebInputException("Invalid input")));

        StepVerifier.create(featureController.updateFeature(1L, feature))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(featureService, times(1)).updateFeature(eq(1L), any(Feature.class));
    }

    @Test
    public void testDeleteFeature_Success() {
        when(featureService.deleteFeature(1L)).thenReturn(Mono.empty());

        StepVerifier.create(featureController.deleteFeature(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NO_CONTENT)
                .verifyComplete();

        verify(featureService, times(1)).deleteFeature(1L);
    }

    @Test
    public void testDeleteFeature_NotFound() {
        when(featureService.deleteFeature(1L))
                .thenReturn(Mono.error(new ServerWebInputException("Feature not found")));

        StepVerifier.create(featureController.deleteFeature(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(featureService, times(1)).deleteFeature(1L);
    }

    @Test
    public void testCreateFeature_ReturnsFeatureWithDates() {
        Feature feature = createFeature(null, 2024L, Quarter.Q1, null, null, "New Feature", "Description", 1L);
        Feature savedFeature = createFeature(1L, 2024L, Quarter.Q1, null, null, "New Feature", "Description", 1L);
        savedFeature.setCreateDate(LocalDateTime.now());
        savedFeature.setUpdateDate(LocalDateTime.now());

        when(featureService.createFeature(any(Feature.class))).thenReturn(Mono.just(savedFeature));

        StepVerifier.create(featureController.createFeature(feature))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getId().equals(1L)
                                && response.getBody().getCreateDate() != null
                                && response.getBody().getUpdateDate() != null)
                .verifyComplete();

        verify(featureService, times(1)).createFeature(any(Feature.class));
    }

    @Test
    public void testUpdateFeature_ReturnsFeatureWithUpdatedDate() {
        LocalDateTime originalCreateDate = LocalDateTime.now().minusDays(5);
        LocalDateTime originalUpdateDate = LocalDateTime.now().minusDays(2);
        Feature existingFeature = createFeature(1L, 2024L, Quarter.Q1, null, null, "Old Feature", "Description", 1L);
        existingFeature.setCreateDate(originalCreateDate);
        existingFeature.setUpdateDate(originalUpdateDate);

        Feature updatedFeature = createFeature(1L, 2024L, Quarter.Q2, null, null, "Updated Feature", "Description", 1L);
        updatedFeature.setCreateDate(originalCreateDate);
        updatedFeature.setUpdateDate(LocalDateTime.now()); // Simulate update

        when(featureService.updateFeature(anyLong(), any(Feature.class)))
                .thenReturn(Mono.just(updatedFeature));

        StepVerifier.create(featureController.updateFeature(1L, existingFeature))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getId().equals(1L)
                                && response.getBody().getCreateDate().equals(originalCreateDate)
                                && response.getBody().getUpdateDate().isAfter(originalUpdateDate))
                .verifyComplete();

        verify(featureService, times(1)).updateFeature(eq(1L), any(Feature.class));
    }

    @Test
    public void testGetFeatureById_ReturnsFeatureWithDates() {
        LocalDateTime createDate = LocalDateTime.now().minusDays(10);
        LocalDateTime updateDate = LocalDateTime.now().minusDays(5);
        Feature feature = createFeature(1L, 2024L, Quarter.Q1, null, null, "Test Feature", "Description", 1L);
        feature.setCreateDate(createDate);
        feature.setUpdateDate(updateDate);

        when(featureService.getFeatureById(1L)).thenReturn(Mono.just(feature));

        StepVerifier.create(featureController.getFeatureById(1L))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getId().equals(1L)
                                && response.getBody().getCreateDate().equals(createDate)
                                && response.getBody().getUpdateDate().equals(updateDate))
                .verifyComplete();

        verify(featureService, times(1)).getFeatureById(1L);
    }

    private Feature createFeature(Long id, Long year, Quarter quarter, Long sprint, String release, String summary,
            String description, Long authorId) {
        return Feature.builder()
                .id(id)
                .year(year)
                .quarter(quarter)
                .sprint(sprint)
                .release(release)
                .summary(summary)
                .description(description)
                .authorId(authorId)
                .build();
    }
}
