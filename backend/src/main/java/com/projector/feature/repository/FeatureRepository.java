package com.projector.feature.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.projector.feature.model.Feature;

import reactor.core.publisher.Flux;

@Repository
public interface FeatureRepository extends R2dbcRepository<Feature, Long> {

    @Query("SELECT * FROM features WHERE :faId = ANY(functional_area_ids)")
    Flux<Feature> findByFunctionalAreaId(Long faId);
}

