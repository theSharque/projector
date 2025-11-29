package com.projector.project.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import com.projector.project.model.Project;
import com.projector.project.model.ProjectUser;
import com.projector.project.repository.ProjectRepository;
import com.projector.project.repository.ProjectUserRepository;

public class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectUserRepository projectUserRepository;

    @InjectMocks private ProjectService projectService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllProjects_Success() {
        Project project1 = createProject(1L, "Project 1", 1L);
        Project project2 = createProject(2L, "Project 2", 1L);

        when(projectRepository.findAll()).thenReturn(Flux.just(project1, project2));
        when(projectUserRepository.findByProjectId(1L)).thenReturn(Flux.empty());
        when(projectUserRepository.findByProjectId(2L)).thenReturn(Flux.empty());

        StepVerifier.create(projectService.getAllProjects())
                .expectNextCount(2)
                .verifyComplete();

        verify(projectRepository, times(1)).findAll();
        verify(projectUserRepository, times(1)).findByProjectId(1L);
        verify(projectUserRepository, times(1)).findByProjectId(2L);
    }

    @Test
    public void testGetAllProjects_WithParticipants() {
        Project project = createProject(1L, "Project 1", 1L);
        ProjectUser participant1 = ProjectUser.builder().projectId(1L).userId(2L).build();
        ProjectUser participant2 = ProjectUser.builder().projectId(1L).userId(3L).build();

        when(projectRepository.findAll()).thenReturn(Flux.just(project));
        when(projectUserRepository.findByProjectId(1L))
                .thenReturn(Flux.just(participant1, participant2));

        StepVerifier.create(projectService.getAllProjects())
                .expectNextMatches(
                        p ->
                                p.getId().equals(1L)
                                        && p.getParticipantIds().size() == 2
                                        && p.getParticipantIds().contains(2L)
                                        && p.getParticipantIds().contains(3L))
                .verifyComplete();

        verify(projectRepository, times(1)).findAll();
        verify(projectUserRepository, times(1)).findByProjectId(1L);
    }

    @Test
    public void testGetProjectById_Success() {
        Project project = createProject(1L, "Test Project", 1L);

        when(projectRepository.findById(1L)).thenReturn(Mono.just(project));
        when(projectUserRepository.findByProjectId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(projectService.getProjectById(1L))
                .expectNextMatches(p -> p.getId().equals(1L) && p.getName().equals("Test Project"))
                .verifyComplete();

        verify(projectRepository, times(1)).findById(1L);
        verify(projectUserRepository, times(1)).findByProjectId(1L);
    }

    @Test
    public void testGetProjectById_NotFound() {
        when(projectRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(projectService.getProjectById(1L))
                .expectErrorMatches(
                        throwable ->
                                throwable instanceof ServerWebInputException
                                        && throwable.getMessage().contains("Project not found"))
                .verify();

        verify(projectRepository, times(1)).findById(1L);
        verify(projectUserRepository, never()).findByProjectId(anyLong());
    }

    @Test
    public void testCreateProject_Success() {
        Project project = createProject(null, "New Project", 1L);
        Project savedProject = createProject(1L, "New Project", 1L);

        when(projectRepository.save(any(Project.class))).thenReturn(Mono.just(savedProject));
        when(projectUserRepository.findByProjectId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(projectService.createProject(project))
                .expectNextMatches(
                        p ->
                                p.getId().equals(1L)
                                        && p.getName().equals("New Project")
                                        && p.getCreateDate() != null)
                .verifyComplete();

        verify(projectRepository, times(1)).save(any(Project.class));
        verify(projectUserRepository, never()).save(any(ProjectUser.class));
        verify(projectUserRepository, times(1)).findByProjectId(1L);
    }

    @Test
    public void testCreateProject_WithParticipants() {
        Project project = createProject(null, "New Project", 1L);
        project.setParticipantIds(Arrays.asList(2L, 3L));
        Project savedProject = createProject(1L, "New Project", 1L);
        ProjectUser participant1 = ProjectUser.builder().projectId(1L).userId(2L).build();
        ProjectUser participant2 = ProjectUser.builder().projectId(1L).userId(3L).build();

        when(projectRepository.save(any(Project.class))).thenReturn(Mono.just(savedProject));
        when(projectUserRepository.save(any(ProjectUser.class)))
                .thenReturn(Mono.just(participant1), Mono.just(participant2));
        when(projectUserRepository.findByProjectId(1L))
                .thenReturn(Flux.just(participant1, participant2));

        StepVerifier.create(projectService.createProject(project))
                .expectNextMatches(
                        p ->
                                p.getId().equals(1L)
                                        && p.getParticipantIds().size() == 2
                                        && p.getParticipantIds().contains(2L)
                                        && p.getParticipantIds().contains(3L))
                .verifyComplete();

        verify(projectRepository, times(1)).save(any(Project.class));
        verify(projectUserRepository, times(2)).save(any(ProjectUser.class));
        verify(projectUserRepository, times(1)).findByProjectId(1L);
    }

    @Test
    public void testCreateProject_EmptyName() {
        Project project = createProject(null, "", 1L);

        StepVerifier.create(projectService.createProject(project))
                .expectErrorMatches(
                        throwable ->
                                throwable instanceof ServerWebInputException
                                        && throwable.getMessage().contains("name cannot be empty"))
                .verify();

        verify(projectRepository, never()).save(any(Project.class));
        verify(projectUserRepository, never()).save(any(ProjectUser.class));
    }

    @Test
    public void testCreateProject_NullName() {
        Project project = createProject(null, null, 1L);

        StepVerifier.create(projectService.createProject(project))
                .expectErrorMatches(
                        throwable ->
                                throwable instanceof ServerWebInputException
                                        && throwable.getMessage().contains("name cannot be empty"))
                .verify();

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    public void testCreateProject_NullAuthor() {
        Project project = createProject(null, "New Project", null);

        StepVerifier.create(projectService.createProject(project))
                .expectErrorMatches(
                        throwable ->
                                throwable instanceof ServerWebInputException
                                        && throwable.getMessage().contains("author is required"))
                .verify();

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    public void testUpdateProject_Success() {
        LocalDateTime createDate = LocalDateTime.now().minusDays(1);
        Project existingProject = createProject(1L, "Old Project", 1L);
        existingProject.setCreateDate(createDate);
        Project updateData = createProject(null, "Updated Project", 1L);
        updateData.setParticipantIds(null);
        Project updatedProject = createProject(1L, "Updated Project", 1L);
        updatedProject.setCreateDate(createDate);

        when(projectRepository.findById(1L)).thenReturn(Mono.just(existingProject));
        when(projectRepository.save(any(Project.class))).thenReturn(Mono.just(updatedProject));
        when(projectUserRepository.findByProjectId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(projectService.updateProject(1L, updateData))
                .expectNextMatches(
                        p ->
                                p.getId() != null
                                        && p.getId().equals(1L)
                                        && p.getName() != null
                                        && p.getName().equals("Updated Project"))
                .verifyComplete();

        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(projectUserRepository, never()).deleteByProjectId(anyLong());
        verify(projectUserRepository, times(1)).findByProjectId(1L);
    }

    @Test
    public void testUpdateProject_WithParticipants() {
        Project existingProject = createProject(1L, "Old Project", 1L);
        existingProject.setCreateDate(LocalDateTime.now().minusDays(1));
        Project updateData = createProject(null, "Updated Project", 1L);
        updateData.setParticipantIds(Arrays.asList(2L, 3L));
        Project updatedProject = createProject(1L, "Updated Project", 1L);
        updatedProject.setCreateDate(existingProject.getCreateDate());
        ProjectUser participant1 = ProjectUser.builder().projectId(1L).userId(2L).build();
        ProjectUser participant2 = ProjectUser.builder().projectId(1L).userId(3L).build();

        when(projectRepository.findById(1L)).thenReturn(Mono.just(existingProject));
        when(projectRepository.save(any(Project.class))).thenReturn(Mono.just(updatedProject));
        when(projectUserRepository.deleteByProjectId(1L)).thenReturn(Mono.just(2));
        when(projectUserRepository.save(any(ProjectUser.class)))
                .thenReturn(Mono.just(participant1), Mono.just(participant2));
        when(projectUserRepository.findByProjectId(1L))
                .thenReturn(Flux.just(participant1, participant2));

        StepVerifier.create(projectService.updateProject(1L, updateData))
                .expectNextMatches(
                        p ->
                                p.getId().equals(1L)
                                        && p.getParticipantIds().size() == 2
                                        && p.getParticipantIds().contains(2L)
                                        && p.getParticipantIds().contains(3L))
                .verifyComplete();

        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(projectUserRepository, times(1)).deleteByProjectId(1L);
        verify(projectUserRepository, times(2)).save(any(ProjectUser.class));
        verify(projectUserRepository, times(1)).findByProjectId(1L);
    }

    @Test
    public void testUpdateProject_NotFound() {
        Project updateData = createProject(null, "Updated Project", 1L);

        when(projectRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(projectService.updateProject(1L, updateData))
                .expectErrorMatches(
                        throwable ->
                                throwable instanceof ServerWebInputException
                                        && throwable.getMessage().contains("Project not found"))
                .verify();

        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    public void testUpdateProject_EmptyName() {
        Project updateData = createProject(null, "", 1L);

        StepVerifier.create(projectService.updateProject(1L, updateData))
                .expectErrorMatches(
                        throwable ->
                                throwable instanceof ServerWebInputException
                                        && throwable.getMessage().contains("name cannot be empty"))
                .verify();

        verify(projectRepository, never()).findById(anyLong());
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    public void testDeleteProject_Success() {
        Project project = createProject(1L, "Test Project", 1L);

        when(projectRepository.findById(1L)).thenReturn(Mono.just(project));
        when(projectUserRepository.deleteByProjectId(1L)).thenReturn(Mono.just(1));
        when(projectRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(projectService.deleteProject(1L)).verifyComplete();

        verify(projectRepository, times(1)).findById(1L);
        verify(projectUserRepository, times(1)).deleteByProjectId(1L);
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteProject_NotFound() {
        when(projectRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(projectService.deleteProject(1L))
                .expectErrorMatches(
                        throwable ->
                                throwable instanceof ServerWebInputException
                                        && throwable.getMessage().contains("Project not found"))
                .verify();

        verify(projectRepository, times(1)).findById(1L);
        verify(projectUserRepository, never()).deleteByProjectId(anyLong());
        verify(projectRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testLoadParticipants_EmptyList() {
        Project project = createProject(1L, "Test Project", 1L);

        when(projectRepository.findAll()).thenReturn(Flux.just(project));
        when(projectUserRepository.findByProjectId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(projectService.getAllProjects())
                .expectNextMatches(
                        p ->
                                p.getId().equals(1L)
                                        && (p.getParticipantIds() == null
                                                || p.getParticipantIds().isEmpty()))
                .verifyComplete();

        verify(projectRepository, times(1)).findAll();
        verify(projectUserRepository, times(1)).findByProjectId(1L);
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
