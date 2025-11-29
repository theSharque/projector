package com.projector.project.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.projector.project.model.ProjectUser;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProjectUserRepository extends R2dbcRepository<ProjectUser, Long> {

    @Query("SELECT * FROM project_users WHERE project_id = :projectId")
    Flux<ProjectUser> findByProjectId(Long projectId);

    @Modifying
    @Query("DELETE FROM project_users WHERE project_id = :projectId")
    Mono<Integer> deleteByProjectId(Long projectId);
}
