package com.projector.task.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.projector.task.model.Task;

@Repository
public interface TaskRepository extends R2dbcRepository<Task, Long> {
}

