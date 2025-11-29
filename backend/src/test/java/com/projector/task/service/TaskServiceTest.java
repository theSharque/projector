package com.projector.task.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ServerWebInputException;

import com.projector.task.model.Task;
import com.projector.task.repository.TaskRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllTasks_Success() {
        Task task1 = createTask(1L, 1L, "Task 1", "Description 1", 1L);
        Task task2 = createTask(2L, 1L, "Task 2", "Description 2", 1L);

        when(taskRepository.findAll()).thenReturn(Flux.just(task1, task2));

        StepVerifier.create(taskService.getAllTasks())
                .expectNextCount(2)
                .verifyComplete();

        verify(taskRepository, times(1)).findAll();
    }

    @Test
    public void testGetTaskById_Success() {
        Task task = createTask(1L, 1L, "Test Task", "Test Description", 1L);

        when(taskRepository.findById(1L)).thenReturn(Mono.just(task));

        StepVerifier.create(taskService.getTaskById(1L))
                .expectNextMatches(t -> t.getId().equals(1L) && t.getFeatureId().equals(1L))
                .verifyComplete();

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetTaskById_NotFound() {
        when(taskRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(taskService.getTaskById(1L))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("Task not found"))
                .verify();

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreateTask_Success() {
        Task task = createTask(null, 1L, "New Task", "New Description", 1L);
        task.setCreateDate(null);
        task.setUpdateDate(null);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(1L);
            return Mono.just(saved);
        });

        StepVerifier.create(taskService.createTask(task))
                .expectNextMatches(
                        t -> t.getId().equals(1L)
                                && t.getFeatureId().equals(1L)
                                && t.getSummary().equals("New Task")
                                && t.getAuthorId().equals(1L)
                                && t.getCreateDate() != null
                                && t.getUpdateDate() != null
                                && t.getCreateDate().equals(t.getUpdateDate()))
                .verifyComplete();

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    public void testCreateTask_NullFeatureId() {
        Task task = createTask(null, null, "Task", "Description", 1L);

        StepVerifier.create(taskService.createTask(task))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("feature ID is required"))
                .verify();

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    public void testCreateTask_NullAuthor() {
        Task task = createTask(null, 1L, "Task", "Description", null);

        StepVerifier.create(taskService.createTask(task))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("author is required"))
                .verify();

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    public void testUpdateTask_Success() {
        LocalDateTime originalCreateDate = LocalDateTime.now().minusDays(5);
        Task existingTask = createTask(1L, 1L, "Old Task", "Old Description", 1L);
        existingTask.setCreateDate(originalCreateDate);
        existingTask.setUpdateDate(LocalDateTime.now().minusDays(2));

        Task updateData = createTask(null, 1L, "Updated Task", "Updated Description", 1L);

        when(taskRepository.findById(1L)).thenReturn(Mono.just(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            return Mono.just(saved);
        });

        LocalDateTime beforeUpdate = LocalDateTime.now();

        StepVerifier.create(taskService.updateTask(1L, updateData))
                .expectNextMatches(
                        t -> {
                            LocalDateTime afterUpdate = LocalDateTime.now();
                            return t.getId().equals(1L)
                                    && t.getFeatureId().equals(1L)
                                    && t.getSummary().equals("Updated Task")
                                    && t.getCreateDate() != null
                                    && t.getCreateDate().equals(originalCreateDate)
                                    && t.getUpdateDate() != null
                                    && !t.getUpdateDate().isBefore(beforeUpdate)
                                    && !t.getUpdateDate().isAfter(afterUpdate)
                                    && t.getUpdateDate().isAfter(t.getCreateDate());
                        })
                .verifyComplete();

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    public void testUpdateTask_NotFound() {
        Task updateData = createTask(null, 1L, "Task", "Description", 1L);

        when(taskRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(taskService.updateTask(1L, updateData))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("Task not found"))
                .verify();

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    public void testDeleteTask_Success() {
        Task task = createTask(1L, 1L, "Task", "Description", 1L);

        when(taskRepository.findById(1L)).thenReturn(Mono.just(task));
        when(taskRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(taskService.deleteTask(1L)).verifyComplete();

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteTask_NotFound() {
        when(taskRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(taskService.deleteTask(1L))
                .expectErrorMatches(
                        throwable -> throwable instanceof ServerWebInputException
                                && throwable.getMessage().contains("Task not found"))
                .verify();

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testCreateTask_SetsCreateDateAndUpdateDate() {
        Task task = createTask(null, 1L, "Task", "Description", 1L);
        task.setCreateDate(null);
        task.setUpdateDate(null);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(1L);
            return Mono.just(saved);
        });

        StepVerifier.create(taskService.createTask(task))
                .expectNextMatches(
                        t -> t.getCreateDate() != null
                                && t.getUpdateDate() != null
                                && t.getCreateDate().equals(t.getUpdateDate()))
                .verifyComplete();

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    public void testUpdateTask_UpdatesUpdateDateButNotCreateDate() {
        LocalDateTime originalCreateDate = LocalDateTime.now().minusDays(10);
        Task existingTask = createTask(1L, 1L, "Old Task", "Description", 1L);
        existingTask.setCreateDate(originalCreateDate);
        existingTask.setUpdateDate(LocalDateTime.now().minusDays(2));

        Task updateData = createTask(null, 1L, "Updated Task", "Description", 1L);
        updateData.setCreateDate(LocalDateTime.now().minusDays(1)); // Attempt to change createDate

        when(taskRepository.findById(1L)).thenReturn(Mono.just(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            return Mono.just(saved);
        });

        LocalDateTime beforeUpdate = LocalDateTime.now();

        StepVerifier.create(taskService.updateTask(1L, updateData))
                .expectNextMatches(
                        t -> {
                            LocalDateTime afterUpdate = LocalDateTime.now();
                            return t.getCreateDate() != null
                                    && t.getUpdateDate() != null
                                    && t.getCreateDate().equals(originalCreateDate)
                                    && !t.getUpdateDate().isBefore(beforeUpdate)
                                    && !t.getUpdateDate().isAfter(afterUpdate)
                                    && t.getUpdateDate().isAfter(t.getCreateDate());
                        })
                .verifyComplete();

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    private Task createTask(Long id, Long featureId, String summary, String description, Long authorId) {
        Task task = Task.builder()
                .id(id)
                .featureId(featureId)
                .summary(summary)
                .description(description)
                .authorId(authorId)
                .build();
        if (id != null) {
            task.setCreateDate(LocalDateTime.now());
        }
        return task;
    }
}

