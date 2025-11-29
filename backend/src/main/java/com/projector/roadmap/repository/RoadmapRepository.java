package com.projector.roadmap.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.projector.roadmap.model.Roadmap;

@Repository
public interface RoadmapRepository extends R2dbcRepository<Roadmap, Long> {
}
