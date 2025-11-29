package com.projector.user.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.projector.user.model.User;

import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    @Query("SELECT * FROM users WHERE lower(email) = lower(:email)")
    Mono<User> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);
}
