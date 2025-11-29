package com.projector.project.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebInputException;

import com.projector.project.model.Project;
import com.projector.project.model.ProjectUser;
import com.projector.project.repository.ProjectRepository;
import com.projector.project.repository.ProjectUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;

    public Flux<Project> getAllProjects() {
        return projectRepository.findAll()
                .flatMap(this::loadParticipants);
    }

    public Mono<Project> getProjectById(Long id) {
        return projectRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Project not found")))
                .flatMap(this::loadParticipants);
    }

    @Transactional
    public Mono<Project> createProject(Project project) {
        return validateProject(project)
                .flatMap(valid -> {
                    project.setId(null);
                    project.setCreateDate(LocalDateTime.now());
                    return projectRepository.save(project);
                })
                .flatMap(savedProject -> {
                    if (project.getParticipantIds() != null && !project.getParticipantIds().isEmpty()) {
                        return assignParticipantsToProject(savedProject.getId(), project.getParticipantIds())
                                .thenReturn(savedProject);
                    }
                    return Mono.just(savedProject);
                })
                .flatMap(this::loadParticipants);
    }

    @Transactional
    public Mono<Project> updateProject(Long id, Project project) {
        return validateProject(project)
                .flatMap(valid -> projectRepository.findById(id))
                .switchIfEmpty(Mono.error(new ServerWebInputException("Project not found")))
                .flatMap(existingProject -> {
                    project.setId(id);
                    project.setCreateDate(existingProject.getCreateDate());
                    return projectRepository.save(project);
                })
                .flatMap(updatedProject -> {
                    if (project.getParticipantIds() != null) {
                        return deleteProjectParticipants(id)
                                .then(assignParticipantsToProject(id, project.getParticipantIds()))
                                .thenReturn(updatedProject);
                    }
                    return Mono.just(updatedProject);
                })
                .flatMap(this::loadParticipants);
    }

    @Transactional
    public Mono<Void> deleteProject(Long id) {
        return projectRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Project not found")))
                .flatMap(project -> deleteProjectParticipants(id)
                        .then(projectRepository.deleteById(id)))
                .then();
    }

    private Mono<Boolean> validateProject(Project project) {
        if (project.getName() == null || project.getName().isBlank()) {
            return Mono.error(new ServerWebInputException("Project name cannot be empty"));
        }

        if (project.getAuthorId() == null) {
            return Mono.error(new ServerWebInputException("Project author is required"));
        }

        return Mono.just(true);
    }

    private Mono<Project> loadParticipants(Project project) {
        Long projectId = project.getId();
        if (projectId == null) {
            return Mono.just(project);
        }
        return projectUserRepository
                .findByProjectId(projectId)
                .map(ProjectUser::getUserId)
                .collectList()
                .map(participantIds -> {
                    project.setParticipantIds(participantIds);
                    return project;
                });
    }

    private Mono<Void> assignParticipantsToProject(Long projectId, List<Long> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(participantIds)
                .map(userId -> ProjectUser.builder()
                        .projectId(projectId)
                        .userId(userId)
                        .build())
                .flatMap(projectUserRepository::save)
                .then();
    }

    private Mono<Void> deleteProjectParticipants(Long projectId) {
        return projectUserRepository.deleteByProjectId(projectId).then();
    }
}
