package com.projector.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import com.projector.project.model.Project;
import com.projector.project.service.ProjectService;

public class ProjectControllerTest {

    @Mock private ProjectService projectService;

    @InjectMocks private ProjectController projectController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllProjects_Success() {
        Project project1 = createProject(1L, "Project 1", 1L);
        Project project2 = createProject(2L, "Project 2", 1L);

        when(projectService.getAllProjects()).thenReturn(Flux.just(project1, project2));

        StepVerifier.create(projectController.getAllProjects())
                .expectNext(project1)
                .expectNext(project2)
                .verifyComplete();

        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    public void testGetProjectById_Success() {
        Project project = createProject(1L, "Test Project", 1L);

        when(projectService.getProjectById(1L)).thenReturn(Mono.just(project));

        StepVerifier.create(projectController.getProjectById(1L))
                .expectNextMatches(
                        response ->
                                response.getStatusCode() == HttpStatus.OK
                                        && response.getBody() != null
                                        && response.getBody().equals(project))
                .verifyComplete();

        verify(projectService, times(1)).getProjectById(1L);
    }

    @Test
    public void testGetProjectById_NotFound() {
        when(projectService.getProjectById(1L))
                .thenReturn(Mono.error(new ServerWebInputException("Project not found")));

        StepVerifier.create(projectController.getProjectById(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(projectService, times(1)).getProjectById(1L);
    }

    @Test
    public void testCreateProject_Success() {
        Project project = createProject(null, "New Project", 1L);
        Project savedProject = createProject(1L, "New Project", 1L);

        when(projectService.createProject(any(Project.class))).thenReturn(Mono.just(savedProject));

        StepVerifier.create(projectController.createProject(project))
                .expectNextMatches(
                        response ->
                                response.getStatusCode() == HttpStatus.OK
                                        && response.getBody() != null
                                        && response.getBody().equals(savedProject))
                .verifyComplete();

        verify(projectService, times(1)).createProject(any(Project.class));
    }

    @Test
    public void testCreateProject_Error() {
        Project project = createProject(null, "New Project", 1L);

        when(projectService.createProject(any(Project.class)))
                .thenReturn(Mono.error(new ServerWebInputException("Invalid input")));

        StepVerifier.create(projectController.createProject(project))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(projectService, times(1)).createProject(any(Project.class));
    }

    @Test
    public void testUpdateProject_Success() {
        Project project = createProject(1L, "Updated Project", 1L);

        when(projectService.updateProject(anyLong(), any(Project.class)))
                .thenReturn(Mono.just(project));

        StepVerifier.create(projectController.updateProject(1L, project))
                .expectNextMatches(
                        response ->
                                response.getStatusCode() == HttpStatus.OK
                                        && response.getBody() != null
                                        && response.getBody().equals(project))
                .verifyComplete();

        verify(projectService, times(1)).updateProject(eq(1L), any(Project.class));
    }

    @Test
    public void testUpdateProject_NotFound() {
        Project project = createProject(1L, "Updated Project", 1L);

        when(projectService.updateProject(anyLong(), any(Project.class)))
                .thenReturn(Mono.error(new ServerWebInputException("Project not found")));

        StepVerifier.create(projectController.updateProject(1L, project))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(projectService, times(1)).updateProject(eq(1L), any(Project.class));
    }

    @Test
    public void testUpdateProject_Error() {
        Project project = createProject(1L, "Updated Project", 1L);

        when(projectService.updateProject(anyLong(), any(Project.class)))
                .thenReturn(Mono.error(new ServerWebInputException("Invalid input")));

        StepVerifier.create(projectController.updateProject(1L, project))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(projectService, times(1)).updateProject(eq(1L), any(Project.class));
    }

    @Test
    public void testDeleteProject_Success() {
        when(projectService.deleteProject(1L)).thenReturn(Mono.empty());

        StepVerifier.create(projectController.deleteProject(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NO_CONTENT)
                .verifyComplete();

        verify(projectService, times(1)).deleteProject(1L);
    }

    @Test
    public void testDeleteProject_NotFound() {
        when(projectService.deleteProject(1L))
                .thenReturn(Mono.error(new ServerWebInputException("Project not found")));

        StepVerifier.create(projectController.deleteProject(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(projectService, times(1)).deleteProject(1L);
    }

    private Project createProject(Long id, String name, Long authorId) {
        Project project = Project.builder()
                .id(id)
                .name(name)
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

