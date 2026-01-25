package com.projector.functionalarea.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.projector.functionalarea.model.FunctionalArea;

@Repository
public interface FunctionalAreaRepository extends R2dbcRepository<FunctionalArea, Long> {
}
