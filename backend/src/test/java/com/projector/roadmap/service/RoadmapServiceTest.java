package com.projector.roadmap.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ServerWebInputException;

import com.projector.roadmap.model.Roadmap;
import com.projector.roadmap.model.RoadmapUser;
import com.projector.roadmap.repository.RoadmapRepository;
import com.projector.roadmap.repository.RoadmapUserRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class RoadmapServiceTest {

    @Mock
    private RoadmapRepository roadmapRepository;
    @Mock
    private RoadmapUserRepository roadmapUserRepository;

    @InjectMocks
    private RoadmapService roadmapService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllRoadmaps_Success() {
        Roadmap project1 = createRoadmap(1L, "Roadmap 1", 1L);
        Roadmap project2 = createRoadmap(2L, "Roadmap 2", 1L);

        when(roadmapRepository.findAll()).thenReturn(Flux.just(project1, project2));
        when(roadmapUserRepository.findByRoadmapId(1L)).thenReturn(Flux.empty());
        when(roadmapUserRepository.findByRoadmapId(2L)).thenReturn(Flux.empty());

        StepVerifier.create(roadmapService.getAllRoadmaps())
                .expectNextCount(2)
                .verifyComplete();

        verify(roadmapRepository, times(1)).findAll();
        verify(roadmapUserRepository, times(1)).findByRoadmapId(1L);
        verify(roadmapUserRepository, times(1)).findByRoadmapId(2L);
    }

    @Test
    public void testGetAllRoadmaps_WithParticipants() {
        Roadmap project = createRoadmap(1L, "Roadmap 1", 1L);
        RoadmapUser participant1 = RoadmapUser.builder().roadmapId(1L).userId(2L).build();
        RoadmapUser participant2 = RoadmapUser.builder().roadmapId(1L).userId(3L).build();

        when(roadmapRepository.findAll()).thenReturn(Flux.just(project));
        when(roadmapUserRepository.findByRoadmapId(1L))
                .thenReturn(Flux.just(participant1, participant2));

        StepVerifier.create(roadmapService.getAllRoadmaps())
                .expectNextMatches(
                        p -> p.getId().equals(1L)
                                && p.getParticipantIds().size() == 2
                                && p.getParticipantIds().contains(2L)
                                && p.getParticipantIds().contains(3L))
                .verifyComplete();

        verify(roadmapRepository, times(1)).findAll();
        verify(roadmapUserRepository, times(1)).findByRoadmapId(1L);
    }

    @Test
    public void testGetRoadmapById_Success() {
        Roadmap project = createRoadmap(1L, "Test Project", 1L);

        when(roadmapRepository.findById(1L)).thenReturn(Mono.just(project));
        when(roadmapUserRepository.findByRoadmapId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(roadmapService.getRoadmapById(1L))
                .expectNextMatches(p -> p.getId().equals(1L) && p.getProjectName().equals("Test Project"))
                .verifyComplete();

        verify(roadmapRepository, times(1)).findById(1L);
        verify(roadmapUserRepository, times(1)).findByRoadmapId(1L);
    }

    @Test
    public void testGetRoadmapById_NotFound() {
        when(roadmapRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(roadmapService.getRoadmapById(1L))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("Roadmap not found"))
                .verify();

        verify(roadmapRepository, times(1)).findById(1L);
        verify(roadmapUserRepository, never()).findByRoadmapId(anyLong());
    }

    @Test
    public void testCreateRoadmap_Success() {
        Roadmap project = createRoadmap(null, "New Project", 1L);
        project.setCreateDate(null); // Ensure createDate is null before service call
        Roadmap savedRoadmap = createRoadmap(1L, "New Project", 1L);

        when(roadmapRepository.save(any(Roadmap.class))).thenAnswer(invocation -> {
            Roadmap saved = invocation.getArgument(0);
            saved.setId(1L);
            return Mono.just(saved);
        });
        when(roadmapUserRepository.findByRoadmapId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(roadmapService.createRoadmap(project))
                .expectNextMatches(
                        p -> p.getId().equals(1L)
                                && p.getProjectName().equals("New Project")
                                && p.getCreateDate() != null
                                && p.getUpdateDate() != null)
                .verifyComplete();

        verify(roadmapRepository, times(1)).save(any(Roadmap.class));
        verify(roadmapUserRepository, never()).save(any(RoadmapUser.class));
        verify(roadmapUserRepository, times(1)).findByRoadmapId(1L);
    }

    @Test
    public void testCreateRoadmap_WithParticipants() {
        Roadmap project = createRoadmap(null, "New Project", 1L);
        project.setParticipantIds(Arrays.asList(2L, 3L));
        Roadmap savedRoadmap = createRoadmap(1L, "New Project", 1L);
        RoadmapUser participant1 = RoadmapUser.builder().roadmapId(1L).userId(2L).build();
        RoadmapUser participant2 = RoadmapUser.builder().roadmapId(1L).userId(3L).build();

        when(roadmapRepository.save(any(Roadmap.class))).thenReturn(Mono.just(savedRoadmap));
        when(roadmapUserRepository.save(any(RoadmapUser.class)))
                .thenReturn(Mono.just(participant1), Mono.just(participant2));
        when(roadmapUserRepository.findByRoadmapId(1L))
                .thenReturn(Flux.just(participant1, participant2));

        StepVerifier.create(roadmapService.createRoadmap(project))
                .expectNextMatches(
                        p -> p.getId().equals(1L)
                                && p.getParticipantIds().size() == 2
                                && p.getParticipantIds().contains(2L)
                                && p.getParticipantIds().contains(3L))
                .verifyComplete();

        verify(roadmapRepository, times(1)).save(any(Roadmap.class));
        verify(roadmapUserRepository, times(2)).save(any(RoadmapUser.class));
        verify(roadmapUserRepository, times(1)).findByRoadmapId(1L);
    }

    @Test
    public void testCreateRoadmap_EmptyName() {
        Roadmap project = createRoadmap(null, "", 1L);

        StepVerifier.create(roadmapService.createRoadmap(project))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("name cannot be empty"))
                .verify();

        verify(roadmapRepository, never()).save(any(Roadmap.class));
        verify(roadmapUserRepository, never()).save(any(RoadmapUser.class));
    }

    @Test
    public void testCreateRoadmap_NullName() {
        Roadmap project = createRoadmap(null, null, 1L);

        StepVerifier.create(roadmapService.createRoadmap(project))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("name cannot be empty"))
                .verify();

        verify(roadmapRepository, never()).save(any(Roadmap.class));
    }

    @Test
    public void testCreateRoadmap_NullAuthor() {
        Roadmap project = createRoadmap(null, "New Project", null);

        StepVerifier.create(roadmapService.createRoadmap(project))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("author is required"))
                .verify();

        verify(roadmapRepository, never()).save(any(Roadmap.class));
    }

    @Test
    public void testUpdateRoadmap_Success() {
        LocalDateTime createDate = LocalDateTime.now().minusDays(1);
        Roadmap existingRoadmap = createRoadmap(1L, "Old Project", 1L);
        existingRoadmap.setCreateDate(createDate);
        Roadmap updateData = createRoadmap(null, "Updated Project", 1L);
        updateData.setParticipantIds(null);
        Roadmap updatedRoadmap = createRoadmap(1L, "Updated Project", 1L);
        updatedRoadmap.setCreateDate(createDate);

        when(roadmapRepository.findById(1L)).thenReturn(Mono.just(existingRoadmap));
        when(roadmapRepository.save(any(Roadmap.class))).thenReturn(Mono.just(updatedRoadmap));
        when(roadmapUserRepository.findByRoadmapId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(roadmapService.updateRoadmap(1L, updateData))
                .expectNextMatches(
                        p -> p.getId() != null
                                && p.getId().equals(1L)
                                && p.getProjectName() != null
                                && p.getProjectName().equals("Updated Project"))
                .verifyComplete();

        verify(roadmapRepository, times(1)).findById(1L);
        verify(roadmapRepository, times(1)).save(any(Roadmap.class));
        verify(roadmapUserRepository, never()).deleteByRoadmapId(anyLong());
        verify(roadmapUserRepository, times(1)).findByRoadmapId(1L);
    }

    @Test
    public void testUpdateRoadmap_WithParticipants() {
        Roadmap existingRoadmap = createRoadmap(1L, "Old Project", 1L);
        existingRoadmap.setCreateDate(LocalDateTime.now().minusDays(1));
        Roadmap updateData = createRoadmap(null, "Updated Project", 1L);
        updateData.setParticipantIds(Arrays.asList(2L, 3L));
        Roadmap updatedRoadmap = createRoadmap(1L, "Updated Project", 1L);
        updatedRoadmap.setCreateDate(existingRoadmap.getCreateDate());
        RoadmapUser participant1 = RoadmapUser.builder().roadmapId(1L).userId(2L).build();
        RoadmapUser participant2 = RoadmapUser.builder().roadmapId(1L).userId(3L).build();

        when(roadmapRepository.findById(1L)).thenReturn(Mono.just(existingRoadmap));
        when(roadmapRepository.save(any(Roadmap.class))).thenReturn(Mono.just(updatedRoadmap));
        when(roadmapUserRepository.deleteByRoadmapId(1L)).thenReturn(Mono.just(2));
        when(roadmapUserRepository.save(any(RoadmapUser.class)))
                .thenReturn(Mono.just(participant1), Mono.just(participant2));
        when(roadmapUserRepository.findByRoadmapId(1L))
                .thenReturn(Flux.just(participant1, participant2));

        StepVerifier.create(roadmapService.updateRoadmap(1L, updateData))
                .expectNextMatches(
                        p -> p.getId().equals(1L)
                                && p.getParticipantIds().size() == 2
                                && p.getParticipantIds().contains(2L)
                                && p.getParticipantIds().contains(3L))
                .verifyComplete();

        verify(roadmapRepository, times(1)).findById(1L);
        verify(roadmapRepository, times(1)).save(any(Roadmap.class));
        verify(roadmapUserRepository, times(1)).deleteByRoadmapId(1L);
        verify(roadmapUserRepository, times(2)).save(any(RoadmapUser.class));
        verify(roadmapUserRepository, times(1)).findByRoadmapId(1L);
    }

    @Test
    public void testUpdateRoadmap_NotFound() {
        Roadmap updateData = createRoadmap(null, "Updated Project", 1L);

        when(roadmapRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(roadmapService.updateRoadmap(1L, updateData))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("Roadmap not found"))
                .verify();

        verify(roadmapRepository, times(1)).findById(1L);
        verify(roadmapRepository, never()).save(any(Roadmap.class));
    }

    @Test
    public void testUpdateRoadmap_EmptyName() {
        Roadmap updateData = createRoadmap(null, "", 1L);

        StepVerifier.create(roadmapService.updateRoadmap(1L, updateData))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("name cannot be empty"))
                .verify();

        verify(roadmapRepository, never()).findById(anyLong());
        verify(roadmapRepository, never()).save(any(Roadmap.class));
    }

    @Test
    public void testDeleteRoadmap_Success() {
        Roadmap project = createRoadmap(1L, "Test Project", 1L);

        when(roadmapRepository.findById(1L)).thenReturn(Mono.just(project));
        when(roadmapUserRepository.deleteByRoadmapId(1L)).thenReturn(Mono.just(1));
        when(roadmapRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(roadmapService.deleteRoadmap(1L)).verifyComplete();

        verify(roadmapRepository, times(1)).findById(1L);
        verify(roadmapUserRepository, times(1)).deleteByRoadmapId(1L);
        verify(roadmapRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteRoadmap_NotFound() {
        when(roadmapRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(roadmapService.deleteRoadmap(1L))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("Roadmap not found"))
                .verify();

        verify(roadmapRepository, times(1)).findById(1L);
        verify(roadmapUserRepository, never()).deleteByRoadmapId(anyLong());
        verify(roadmapRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testCreateRoadmap_SetsCreateDateAndUpdateDate() {
        Roadmap project = createRoadmap(null, "New Project", 1L);
        project.setCreateDate(null);
        project.setUpdateDate(null);

        when(roadmapRepository.save(any(Roadmap.class))).thenAnswer(invocation -> {
            Roadmap saved = invocation.getArgument(0);
            saved.setId(1L);
            return Mono.just(saved);
        });
        when(roadmapUserRepository.findByRoadmapId(1L)).thenReturn(Flux.empty());

        LocalDateTime beforeCreate = LocalDateTime.now();

        StepVerifier.create(roadmapService.createRoadmap(project))
                .expectNextMatches(
                        p -> {
                            LocalDateTime afterCreate = LocalDateTime.now();
                            return p.getCreateDate() != null
                                    && p.getUpdateDate() != null
                                    && !p.getCreateDate().isBefore(beforeCreate)
                                    && !p.getCreateDate().isAfter(afterCreate)
                                    && !p.getUpdateDate().isBefore(beforeCreate)
                                    && !p.getUpdateDate().isAfter(afterCreate)
                                    && p.getCreateDate().equals(p.getUpdateDate());
                        })
                .verifyComplete();

        verify(roadmapRepository, times(1)).save(any(Roadmap.class));
    }

    @Test
    public void testUpdateRoadmap_UpdatesUpdateDateButNotCreateDate() {
        LocalDateTime originalCreateDate = LocalDateTime.now().minusDays(5);
        Roadmap existingRoadmap = createRoadmap(1L, "Old Project", 1L);
        existingRoadmap.setCreateDate(originalCreateDate);
        existingRoadmap.setUpdateDate(LocalDateTime.now().minusDays(2));

        Roadmap updateData = createRoadmap(null, "Updated Project", 1L);
        updateData.setParticipantIds(null);
        // Try to change createDate (should be ignored)
        updateData.setCreateDate(LocalDateTime.now().minusDays(1));

        when(roadmapRepository.findById(1L)).thenReturn(Mono.just(existingRoadmap));
        when(roadmapRepository.save(any(Roadmap.class))).thenAnswer(invocation -> {
            Roadmap saved = invocation.getArgument(0);
            return Mono.just(saved);
        });
        when(roadmapUserRepository.findByRoadmapId(1L)).thenReturn(Flux.empty());

        LocalDateTime beforeUpdate = LocalDateTime.now();

        StepVerifier.create(roadmapService.updateRoadmap(1L, updateData))
                .expectNextMatches(
                        p -> {
                            LocalDateTime afterUpdate = LocalDateTime.now();
                            return p.getCreateDate() != null
                                    && p.getUpdateDate() != null
                                    && p.getCreateDate().equals(originalCreateDate)
                                    && !p.getUpdateDate().isBefore(beforeUpdate)
                                    && !p.getUpdateDate().isAfter(afterUpdate)
                                    && p.getUpdateDate().isAfter(p.getCreateDate());
                        })
                .verifyComplete();

        verify(roadmapRepository, times(1)).findById(1L);
        verify(roadmapRepository, times(1)).save(any(Roadmap.class));
    }

    @Test
    public void testUpdateRoadmap_CreateDateCannotBeChanged() {
        LocalDateTime originalCreateDate = LocalDateTime.now().minusDays(10);
        Roadmap existingRoadmap = createRoadmap(1L, "Old Project", 1L);
        existingRoadmap.setCreateDate(originalCreateDate);

        Roadmap updateData = createRoadmap(null, "Updated Project", 1L);
        updateData.setParticipantIds(null);
        // Attempt to change createDate to a different value
        LocalDateTime attemptedCreateDate = LocalDateTime.now().minusDays(1);
        updateData.setCreateDate(attemptedCreateDate);

        when(roadmapRepository.findById(1L)).thenReturn(Mono.just(existingRoadmap));
        when(roadmapRepository.save(any(Roadmap.class))).thenAnswer(invocation -> {
            Roadmap saved = invocation.getArgument(0);
            return Mono.just(saved);
        });
        when(roadmapUserRepository.findByRoadmapId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(roadmapService.updateRoadmap(1L, updateData))
                .expectNextMatches(
                        p -> p.getCreateDate() != null
                                && p.getCreateDate().equals(originalCreateDate)
                                && !p.getCreateDate().equals(attemptedCreateDate))
                .verifyComplete();

        verify(roadmapRepository, times(1)).findById(1L);
        verify(roadmapRepository, times(1)).save(any(Roadmap.class));
    }

    @Test
    public void testCreateRoadmap_UpdateDateEqualsCreateDate() {
        Roadmap project = createRoadmap(null, "New Project", 1L);
        project.setCreateDate(null);
        project.setUpdateDate(null);

        when(roadmapRepository.save(any(Roadmap.class))).thenAnswer(invocation -> {
            Roadmap saved = invocation.getArgument(0);
            saved.setId(1L);
            return Mono.just(saved);
        });
        when(roadmapUserRepository.findByRoadmapId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(roadmapService.createRoadmap(project))
                .expectNextMatches(
                        p -> p.getCreateDate() != null
                                && p.getUpdateDate() != null
                                && p.getCreateDate().equals(p.getUpdateDate()))
                .verifyComplete();

        verify(roadmapRepository, times(1)).save(any(Roadmap.class));
    }

    @Test
    public void testLoadParticipants_EmptyList() {
        Roadmap project = createRoadmap(1L, "Test Project", 1L);

        when(roadmapRepository.findAll()).thenReturn(Flux.just(project));
        when(roadmapUserRepository.findByRoadmapId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(roadmapService.getAllRoadmaps())
                .expectNextMatches(
                        p -> p.getId().equals(1L)
                                && (p.getParticipantIds() == null
                                        || p.getParticipantIds().isEmpty()))
                .verifyComplete();

        verify(roadmapRepository, times(1)).findAll();
        verify(roadmapUserRepository, times(1)).findByRoadmapId(1L);
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
