package com.projector.role.repository;

import com.projector.role.model.Role;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RoleRepository extends R2dbcRepository<Role, Long> {

    Mono<Boolean> existsByName(String name);

    Mono<Role> findByName(String name);
}
