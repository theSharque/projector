package com.projector.user.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.projector.user.model.UserRole;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRoleRepository extends R2dbcRepository<UserRole, Long> {

    @Query("SELECT * FROM user_roles WHERE user_id = :userId")
    Flux<UserRole> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM user_roles WHERE user_id = :userId")
    Mono<Integer> deleteByUserId(Long userId);

    @Modifying
    @Query("INSERT INTO user_roles (user_id, role_id) VALUES (:userId, :roleId) ON CONFLICT DO NOTHING")
    Mono<Integer> insertUserRole(Long userId, Long roleId);
}
