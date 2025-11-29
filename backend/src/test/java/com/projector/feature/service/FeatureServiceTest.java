package com.projector.feature.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ServerWebInputException;

import com.projector.feature.model.Feature;
import com.projector.feature.model.Quarter;
import com.projector.feature.repository.FeatureRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class FeatureServiceTest {

    @Mock
    private FeatureRepository featureRepository;

    @InjectMocks
    private FeatureService featureService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllFeatures_Success() {
        Feature feature1 = createFeature(1L, 2024L, Quarter.Q1, null, null, "Feature 1", "Description 1", 1L);
        Feature feature2 = createFeature(2L, 2024L, Quarter.Q2, 1L, "v1.0", "Feature 2", "Description 2", 1L);

        when(featureRepository.findAll()).thenReturn(Flux.just(feature1, feature2));

        StepVerifier.create(featureService.getAllFeatures())
                .expectNextCount(2)
                .verifyComplete();

        verify(featureRepository, times(1)).findAll();
    }

    @Test
    public void testGetFeatureById_Success() {
        Feature feature = createFeature(1L, 2024L, Quarter.Q1, null, null, "Test Feature", "Test Description", 1L);

        when(featureRepository.findById(1L)).thenReturn(Mono.just(feature));

        StepVerifier.create(featureService.getFeatureById(1L))
                .expectNextMatches(f -> f.getId().equals(1L) && f.getYear().equals(2024L) && f.getQuarter() == Quarter.Q1)
                .verifyComplete();

        verify(featureRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetFeatureById_NotFound() {
        when(featureRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(featureService.getFeatureById(1L))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("Feature not found"))
                .verify();

        verify(featureRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreateFeature_Success() {
        Feature feature = createFeature(null, 2024L, Quarter.Q1, 1L, "v1.0", "New Feature", "New Description", 1L);
        feature.setCreateDate(null);
        feature.setUpdateDate(null);

        when(featureRepository.save(any(Feature.class))).thenAnswer(invocation -> {
            Feature saved = invocation.getArgument(0);
            saved.setId(1L);
            return Mono.just(saved);
        });

        StepVerifier.create(featureService.createFeature(feature))
                .expectNextMatches(
                        f -> f.getId().equals(1L)
                                && f.getYear().equals(2024L)
                                && f.getQuarter() == Quarter.Q1
                                && f.getSprint().equals(1L)
                                && f.getRelease().equals("v1.0")
                                && f.getAuthorId().equals(1L)
                                && f.getCreateDate() != null
                                && f.getUpdateDate() != null
                                && f.getCreateDate().equals(f.getUpdateDate()))
                .verifyComplete();

        verify(featureRepository, times(1)).save(any(Feature.class));
    }

    @Test
    public void testCreateFeature_NullYear() {
        Feature feature = createFeature(null, null, Quarter.Q1, null, null, "Feature", "Description", 1L);

        StepVerifier.create(featureService.createFeature(feature))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("year is required"))
                .verify();

        verify(featureRepository, never()).save(any(Feature.class));
    }

    @Test
    public void testCreateFeature_YearTooLow() {
        Feature feature = createFeature(null, 1999L, Quarter.Q1, null, null, "Feature", "Description", 1L);

        StepVerifier.create(featureService.createFeature(feature))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("year must be between 2000 and 2500"))
                .verify();

        verify(featureRepository, never()).save(any(Feature.class));
    }

    @Test
    public void testCreateFeature_YearTooHigh() {
        Feature feature = createFeature(null, 2501L, Quarter.Q1, null, null, "Feature", "Description", 1L);

        StepVerifier.create(featureService.createFeature(feature))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("year must be between 2000 and 2500"))
                .verify();

        verify(featureRepository, never()).save(any(Feature.class));
    }

    @Test
    public void testCreateFeature_NullQuarter() {
        Feature feature = createFeature(null, 2024L, null, null, null, "Feature", "Description", 1L);

        StepVerifier.create(featureService.createFeature(feature))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("quarter is required"))
                .verify();

        verify(featureRepository, never()).save(any(Feature.class));
    }

    @Test
    public void testUpdateFeature_Success() {
        LocalDateTime originalCreateDate = LocalDateTime.now().minusDays(5);
        Feature existingFeature = createFeature(1L, 2024L, Quarter.Q1, 1L, "v1.0", "Old Feature", "Old Description", 1L);
        existingFeature.setCreateDate(originalCreateDate);
        existingFeature.setUpdateDate(LocalDateTime.now().minusDays(2));
        Feature updateData = createFeature(null, 2024L, Quarter.Q2, 2L, "v1.1", "Updated Feature", "Updated Description", 1L);

        when(featureRepository.findById(1L)).thenReturn(Mono.just(existingFeature));
        when(featureRepository.save(any(Feature.class))).thenAnswer(invocation -> {
            Feature saved = invocation.getArgument(0);
            return Mono.just(saved);
        });

        LocalDateTime beforeUpdate = LocalDateTime.now();

        StepVerifier.create(featureService.updateFeature(1L, updateData))
                .expectNextMatches(
                        f -> {
                            LocalDateTime afterUpdate = LocalDateTime.now();
                            return f.getId().equals(1L)
                                    && f.getYear().equals(2024L)
                                    && f.getQuarter() == Quarter.Q2
                                    && f.getSprint().equals(2L)
                                    && f.getRelease().equals("v1.1")
                                    && f.getCreateDate() != null
                                    && f.getCreateDate().equals(originalCreateDate)
                                    && f.getUpdateDate() != null
                                    && !f.getUpdateDate().isBefore(beforeUpdate)
                                    && !f.getUpdateDate().isAfter(afterUpdate)
                                    && f.getUpdateDate().isAfter(f.getCreateDate());
                        })
                .verifyComplete();

        verify(featureRepository, times(1)).findById(1L);
        verify(featureRepository, times(1)).save(any(Feature.class));
    }

    @Test
    public void testUpdateFeature_NotFound() {
        Feature updateData = createFeature(null, 2024L, Quarter.Q1, null, null, "Feature", "Description", 1L);

        when(featureRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(featureService.updateFeature(1L, updateData))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("Feature not found"))
                .verify();

        verify(featureRepository, times(1)).findById(1L);
        verify(featureRepository, never()).save(any(Feature.class));
    }

    @Test
    public void testDeleteFeature_Success() {
        Feature feature = createFeature(1L, 2024L, Quarter.Q1, null, null, "Feature", "Description", 1L);

        when(featureRepository.findById(1L)).thenReturn(Mono.just(feature));
        when(featureRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(featureService.deleteFeature(1L)).verifyComplete();

        verify(featureRepository, times(1)).findById(1L);
        verify(featureRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteFeature_NotFound() {
        when(featureRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(featureService.deleteFeature(1L))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("Feature not found"))
                .verify();

        verify(featureRepository, times(1)).findById(1L);
        verify(featureRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testCreateFeature_AllQuarters() {
        for (Quarter quarter : Quarter.values()) {
            Feature feature = createFeature(null, 2024L, quarter, null, null, "Feature " + quarter, "Description", 1L);

            when(featureRepository.save(any(Feature.class))).thenAnswer(invocation -> {
                Feature saved = invocation.getArgument(0);
                saved.setId(1L);
                return Mono.just(saved);
            });

            StepVerifier.create(featureService.createFeature(feature))
                    .expectNextMatches(f -> f.getQuarter() == quarter)
                    .verifyComplete();
        }

        verify(featureRepository, times(4)).save(any(Feature.class));
    }

    @Test
    public void testCreateFeature_NullAuthor() {
        Feature feature = createFeature(null, 2024L, Quarter.Q1, null, null, "Feature", "Description", null);

        StepVerifier.create(featureService.createFeature(feature))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("author is required"))
                .verify();

        verify(featureRepository, never()).save(any(Feature.class));
    }

    @Test
    public void testCreateFeature_SetsCreateDateAndUpdateDate() {
        Feature feature = createFeature(null, 2024L, Quarter.Q1, null, null, "Feature", "Description", 1L);
        feature.setCreateDate(null);
        feature.setUpdateDate(null);

        when(featureRepository.save(any(Feature.class))).thenAnswer(invocation -> {
            Feature saved = invocation.getArgument(0);
            saved.setId(1L);
            return Mono.just(saved);
        });

        StepVerifier.create(featureService.createFeature(feature))
                .expectNextMatches(
                        f -> f.getCreateDate() != null
                                && f.getUpdateDate() != null
                                && f.getCreateDate().equals(f.getUpdateDate()))
                .verifyComplete();

        verify(featureRepository, times(1)).save(any(Feature.class));
    }

    @Test
    public void testUpdateFeature_UpdatesUpdateDateButNotCreateDate() {
        LocalDateTime originalCreateDate = LocalDateTime.now().minusDays(10);
        Feature existingFeature = createFeature(1L, 2024L, Quarter.Q1, null, null, "Old Feature", "Description", 1L);
        existingFeature.setCreateDate(originalCreateDate);
        existingFeature.setUpdateDate(LocalDateTime.now().minusDays(2));

        Feature updateData = createFeature(null, 2024L, Quarter.Q2, null, null, "Updated Feature", "Description", 1L);
        updateData.setCreateDate(LocalDateTime.now().minusDays(1)); // Attempt to change createDate

        when(featureRepository.findById(1L)).thenReturn(Mono.just(existingFeature));
        when(featureRepository.save(any(Feature.class))).thenAnswer(invocation -> {
            Feature saved = invocation.getArgument(0);
            return Mono.just(saved);
        });

        LocalDateTime beforeUpdate = LocalDateTime.now();

        StepVerifier.create(featureService.updateFeature(1L, updateData))
                .expectNextMatches(
                        f -> {
                            LocalDateTime afterUpdate = LocalDateTime.now();
                            return f.getCreateDate() != null
                                    && f.getUpdateDate() != null
                                    && f.getCreateDate().equals(originalCreateDate)
                                    && !f.getUpdateDate().isBefore(beforeUpdate)
                                    && !f.getUpdateDate().isAfter(afterUpdate)
                                    && f.getUpdateDate().isAfter(f.getCreateDate());
                        })
                .verifyComplete();

        verify(featureRepository, times(1)).findById(1L);
        verify(featureRepository, times(1)).save(any(Feature.class));
    }

    private Feature createFeature(Long id, Long year, Quarter quarter, Long sprint, String release, String summary,
            String description, Long authorId) {
        Feature feature = Feature.builder()
                .id(id)
                .year(year)
                .quarter(quarter)
                .sprint(sprint)
                .release(release)
                .summary(summary)
                .description(description)
                .authorId(authorId)
                .build();
        return feature;
    }
}

