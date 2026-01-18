package com.projector.roadmap.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.projector.roadmap.model.RoadmapUser;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RoadmapUserRepository extends R2dbcRepository<RoadmapUser, Long> {

    @Query("SELECT * FROM roadmap_users WHERE roadmap_id = :roadmapId")
    Flux<RoadmapUser> findByRoadmapId(Long roadmapId);

    @Modifying
    @Query("DELETE FROM roadmap_users WHERE roadmap_id = :roadmapId")
    Mono<Integer> deleteByRoadmapId(Long roadmapId);

    @Modifying
    @Query("INSERT INTO roadmap_users (roadmap_id, user_id) VALUES (:roadmapId, :userId)")
    Mono<Integer> insertRoadmapUser(Long roadmapId, Long userId);
}
