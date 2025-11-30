package com.projector.roadmap.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebInputException;

import com.projector.roadmap.model.Roadmap;
import com.projector.roadmap.service.RoadmapService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class RoadmapControllerTest {

    @Mock
    private RoadmapService roadmapService;

    @InjectMocks
    private RoadmapController roadmapController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllRoadmaps_Success() {
        Roadmap project1 = createRoadmap(1L, "Roadmap 1", 1L);
        Roadmap project2 = createRoadmap(2L, "Roadmap 2", 1L);

        when(roadmapService.getAllRoadmaps()).thenReturn(Flux.just(project1, project2));

        StepVerifier.create(roadmapController.getAllRoadmaps())
                .expectNext(project1)
                .expectNext(project2)
                .verifyComplete();

        verify(roadmapService, times(1)).getAllRoadmaps();
    }

    @Test
    public void testGetRoadmapById_Success() {
        Roadmap project = createRoadmap(1L, "Test Project", 1L);

        when(roadmapService.getRoadmapById(1L)).thenReturn(Mono.just(project));

        StepVerifier.create(roadmapController.getRoadmapById(1L))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().equals(project))
                .verifyComplete();

        verify(roadmapService, times(1)).getRoadmapById(1L);
    }

    @Test
    public void testGetRoadmapById_NotFound() {
        when(roadmapService.getRoadmapById(1L))
                .thenReturn(Mono.error(new ServerWebInputException("Roadmap not found")));

        StepVerifier.create(roadmapController.getRoadmapById(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(roadmapService, times(1)).getRoadmapById(1L);
    }

    @Test
    public void testCreateRoadmap_Success() {
        Roadmap project = createRoadmap(null, "New Project", 1L);
        Roadmap savedRoadmap = createRoadmap(1L, "New Project", 1L);

        when(roadmapService.createRoadmap(any(Roadmap.class))).thenReturn(Mono.just(savedRoadmap));

        StepVerifier.create(roadmapController.createRoadmap(project))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().equals(savedRoadmap))
                .verifyComplete();

        verify(roadmapService, times(1)).createRoadmap(any(Roadmap.class));
    }

    @Test
    public void testCreateRoadmap_Error() {
        Roadmap project = createRoadmap(null, "New Project", 1L);

        when(roadmapService.createRoadmap(any(Roadmap.class)))
                .thenReturn(Mono.error(new ServerWebInputException("Invalid input")));

        StepVerifier.create(roadmapController.createRoadmap(project))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(roadmapService, times(1)).createRoadmap(any(Roadmap.class));
    }

    @Test
    public void testUpdateRoadmap_Success() {
        Roadmap project = createRoadmap(1L, "Updated Project", 1L);

        when(roadmapService.updateRoadmap(anyLong(), any(Roadmap.class)))
                .thenReturn(Mono.just(project));

        StepVerifier.create(roadmapController.updateRoadmap(1L, project))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().equals(project))
                .verifyComplete();

        verify(roadmapService, times(1)).updateRoadmap(eq(1L), any(Roadmap.class));
    }

    @Test
    public void testUpdateRoadmap_NotFound() {
        Roadmap project = createRoadmap(1L, "Updated Project", 1L);

        when(roadmapService.updateRoadmap(anyLong(), any(Roadmap.class)))
                .thenReturn(Mono.error(new ServerWebInputException("Roadmap not found")));

        StepVerifier.create(roadmapController.updateRoadmap(1L, project))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(roadmapService, times(1)).updateRoadmap(eq(1L), any(Roadmap.class));
    }

    @Test
    public void testUpdateRoadmap_Error() {
        Roadmap project = createRoadmap(1L, "Updated Project", 1L);

        when(roadmapService.updateRoadmap(anyLong(), any(Roadmap.class)))
                .thenReturn(Mono.error(new ServerWebInputException("Invalid input")));

        StepVerifier.create(roadmapController.updateRoadmap(1L, project))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(roadmapService, times(1)).updateRoadmap(eq(1L), any(Roadmap.class));
    }

    @Test
    public void testDeleteRoadmap_Success() {
        when(roadmapService.deleteRoadmap(1L)).thenReturn(Mono.empty());

        StepVerifier.create(roadmapController.deleteRoadmap(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NO_CONTENT)
                .verifyComplete();

        verify(roadmapService, times(1)).deleteRoadmap(1L);
    }

    @Test
    public void testDeleteRoadmap_NotFound() {
        when(roadmapService.deleteRoadmap(1L))
                .thenReturn(Mono.error(new ServerWebInputException("Roadmap not found")));

        StepVerifier.create(roadmapController.deleteRoadmap(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(roadmapService, times(1)).deleteRoadmap(1L);
    }

    @Test
    public void testCreateRoadmap_ReturnsRoadmapWithDates() {
        Roadmap project = createRoadmap(null, "New Project", 1L);
        Roadmap savedRoadmap = createRoadmap(1L, "New Project", 1L);
        savedRoadmap.setCreateDate(LocalDateTime.now());
        savedRoadmap.setUpdateDate(LocalDateTime.now());

        when(roadmapService.createRoadmap(any(Roadmap.class))).thenReturn(Mono.just(savedRoadmap));

        StepVerifier.create(roadmapController.createRoadmap(project))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getCreateDate() != null
                                && response.getBody().getUpdateDate() != null)
                .verifyComplete();

        verify(roadmapService, times(1)).createRoadmap(any(Roadmap.class));
    }

    @Test
    public void testUpdateRoadmap_ReturnsRoadmapWithUpdatedDate() {
        Roadmap project = createRoadmap(1L, "Updated Project", 1L);
        project.setCreateDate(LocalDateTime.now().minusDays(5));
        project.setUpdateDate(LocalDateTime.now());

        when(roadmapService.updateRoadmap(anyLong(), any(Roadmap.class)))
                .thenReturn(Mono.just(project));

        StepVerifier.create(roadmapController.updateRoadmap(1L, project))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getCreateDate() != null
                                && response.getBody().getUpdateDate() != null
                                && response.getBody().getUpdateDate()
                                        .isAfter(response.getBody().getCreateDate()))
                .verifyComplete();

        verify(roadmapService, times(1)).updateRoadmap(eq(1L), any(Roadmap.class));
    }

    @Test
    public void testGetRoadmapById_ReturnsRoadmapWithDates() {
        Roadmap project = createRoadmap(1L, "Test Project", 1L);
        project.setCreateDate(LocalDateTime.now().minusDays(10));
        project.setUpdateDate(LocalDateTime.now().minusDays(1));

        when(roadmapService.getRoadmapById(1L)).thenReturn(Mono.just(project));

        StepVerifier.create(roadmapController.getRoadmapById(1L))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getCreateDate() != null
                                && response.getBody().getUpdateDate() != null)
                .verifyComplete();

        verify(roadmapService, times(1)).getRoadmapById(1L);
    }

    private Roadmap createRoadmap(Long id, String name, Long authorId) {
        Roadmap project = Roadmap.builder()
                .id(id)
                .projectName(name)
                .authorId(authorId)
                .mission("Test mission")
                .description("Test description")
                .participantIds(Collections.emptyList())
                .build();
        if (id != null) {
            project.setCreateDate(LocalDateTime.now());
        }
        return project;
    }
}
