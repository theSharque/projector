package com.projector.task.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebInputException;

import com.projector.task.model.Task;
import com.projector.task.repository.TaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    public Flux<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Mono<Task> getTaskById(Long id) {
        return taskRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Task not found")));
    }

    @Transactional
    public Mono<Task> createTask(Task task) {
        return validateTask(task)
                .flatMap(valid -> {
                    LocalDateTime now = LocalDateTime.now();
                    task.setId(null);
                    task.setCreateDate(now);
                    task.setUpdateDate(now);
                    return taskRepository.save(task);
                });
    }

    @Transactional
    public Mono<Task> updateTask(Long id, Task task) {
        return validateTask(task)
                .flatMap(valid -> taskRepository.findById(id))
                .switchIfEmpty(Mono.error(new ServerWebInputException("Task not found")))
                .flatMap(existingTask -> {
                    task.setId(id);
                    task.setCreateDate(existingTask.getCreateDate());
                    task.setUpdateDate(LocalDateTime.now());
                    return taskRepository.save(task);
                });
    }

    @Transactional
    public Mono<Void> deleteTask(Long id) {
        return taskRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Task not found")))
                .flatMap(task -> taskRepository.deleteById(id))
                .then();
    }

    private Mono<Boolean> validateTask(Task task) {
        if (task.getFeatureId() == null) {
            return Mono.error(new ServerWebInputException("Task feature ID is required"));
        }

        if (task.getAuthorId() == null) {
            return Mono.error(new ServerWebInputException("Task author is required"));
        }

        return Mono.just(true);
    }
}

