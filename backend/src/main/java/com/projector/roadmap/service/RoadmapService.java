package com.projector.roadmap.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebInputException;

import com.projector.roadmap.model.Roadmap;
import com.projector.roadmap.model.RoadmapUser;
import com.projector.roadmap.repository.RoadmapRepository;
import com.projector.roadmap.repository.RoadmapUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapUserRepository roadmapUserRepository;

    public Flux<Roadmap> getAllRoadmaps() {
        return roadmapRepository.findAll()
                .flatMap(this::loadParticipants);
    }

    public Mono<Roadmap> getRoadmapById(Long id) {
        return roadmapRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Roadmap not found")))
                .flatMap(this::loadParticipants);
    }

    @Transactional
    public Mono<Roadmap> createRoadmap(Roadmap roadmap) {
        return validateRoadmap(roadmap)
                .flatMap(valid -> {
                    LocalDateTime now = LocalDateTime.now();
                    roadmap.setId(null);
                    roadmap.setCreateDate(now);
                    roadmap.setUpdateDate(now);
                    return roadmapRepository.save(roadmap);
                })
                .flatMap(savedRoadmap -> {
                    if (roadmap.getParticipantIds() != null && !roadmap.getParticipantIds().isEmpty()) {
                        return assignParticipantsToRoadmap(savedRoadmap.getId(), roadmap.getParticipantIds())
                                .thenReturn(savedRoadmap);
                    }
                    return Mono.just(savedRoadmap);
                })
                .flatMap(this::loadParticipants);
    }

    @Transactional
    public Mono<Roadmap> updateRoadmap(Long id, Roadmap roadmap) {
        return validateRoadmap(roadmap)
                .flatMap(valid -> roadmapRepository.findById(id))
                .switchIfEmpty(Mono.error(new ServerWebInputException("Roadmap not found")))
                .flatMap(existingRoadmap -> {
                    roadmap.setId(id);
                    roadmap.setCreateDate(existingRoadmap.getCreateDate());
                    roadmap.setUpdateDate(LocalDateTime.now());
                    return roadmapRepository.save(roadmap);
                })
                .flatMap(updatedRoadmap -> {
                    if (roadmap.getParticipantIds() != null) {
                        return deleteRoadmapParticipants(id)
                                .then(assignParticipantsToRoadmap(id, roadmap.getParticipantIds()))
                                .thenReturn(updatedRoadmap);
                    }
                    return Mono.just(updatedRoadmap);
                })
                .flatMap(this::loadParticipants);
    }

    @Transactional
    public Mono<Void> deleteRoadmap(Long id) {
        return roadmapRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Roadmap not found")))
                .flatMap(roadmap -> deleteRoadmapParticipants(id)
                        .then(roadmapRepository.deleteById(id)))
                .then();
    }

    private Mono<Boolean> validateRoadmap(Roadmap roadmap) {
        if (roadmap.getProjectName() == null || roadmap.getProjectName().isBlank()) {
            return Mono.error(new ServerWebInputException("Roadmap project name cannot be empty"));
        }

        if (roadmap.getAuthorId() == null) {
            return Mono.error(new ServerWebInputException("Roadmap author is required"));
        }

        return Mono.just(true);
    }

    private Mono<Roadmap> loadParticipants(Roadmap roadmap) {
        Long roadmapId = roadmap.getId();
        if (roadmapId == null) {
            return Mono.just(roadmap);
        }
        return roadmapUserRepository
                .findByRoadmapId(roadmapId)
                .map(RoadmapUser::getUserId)
                .collectList()
                .map(participantIds -> {
                    roadmap.setParticipantIds(participantIds);
                    return roadmap;
                });
    }

    private Mono<Void> assignParticipantsToRoadmap(Long roadmapId, List<Long> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(participantIds)
                .map(userId -> RoadmapUser.builder()
                        .roadmapId(roadmapId)
                        .userId(userId)
                        .build())
                .flatMap(roadmapUserRepository::save)
                .then();
    }

    private Mono<Void> deleteRoadmapParticipants(Long roadmapId) {
        return roadmapUserRepository.deleteByRoadmapId(roadmapId).then();
    }
}
