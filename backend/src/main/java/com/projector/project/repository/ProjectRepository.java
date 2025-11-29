package com.projector.project.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.projector.project.model.Project;

@Repository
public interface ProjectRepository extends R2dbcRepository<Project, Long> {
}
