package com.projector.feature.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.projector.feature.model.Feature;

@Repository
public interface FeatureRepository extends R2dbcRepository<Feature, Long> {
}

