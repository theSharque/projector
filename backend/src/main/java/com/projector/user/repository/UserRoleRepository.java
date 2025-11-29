package com.projector.user.repository;

import com.projector.user.model.UserRole;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRoleRepository extends R2dbcRepository<UserRole, Long> {

    @Query("SELECT * FROM user_roles WHERE user_id = :userId")
    Flux<UserRole> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM user_roles WHERE user_id = :userId")
    Mono<Integer> deleteByUserId(Long userId);
}

