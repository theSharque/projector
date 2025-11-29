package com.projector.role.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.projector.role.model.Role;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RoleRepository extends R2dbcRepository<Role, Long> {

    Mono<Boolean> existsByName(String name);

    Mono<Role> findByName(String name);

    @Query("SELECT r.* FROM roles r INNER JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = :userId")
    Flux<Role> findByUserId(Long userId);

    @Modifying
    @Query("WITH deleted_user_roles AS (DELETE FROM user_roles WHERE role_id = :roleId) "
            + "DELETE FROM roles WHERE id = :roleId")
    Mono<Integer> deleteCascadeById(Long roleId);
}
