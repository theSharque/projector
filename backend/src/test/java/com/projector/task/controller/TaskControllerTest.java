package com.projector.task.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebInputException;

import com.projector.task.model.Task;
import com.projector.task.service.TaskService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllTasks_Success() {
        Task task1 = createTask(1L, 1L, "Task 1", "Description 1", 1L);
        Task task2 = createTask(2L, 1L, "Task 2", "Description 2", 1L);

        when(taskService.getAllTasks()).thenReturn(Flux.just(task1, task2));

        StepVerifier.create(taskController.getAllTasks())
                .expectNext(task1)
                .expectNext(task2)
                .verifyComplete();

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    public void testGetTaskById_Success() {
        Task task = createTask(1L, 1L, "Test Task", "Test Description", 1L);

        when(taskService.getTaskById(1L)).thenReturn(Mono.just(task));

        StepVerifier.create(taskController.getTaskById(1L))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getId().equals(1L)
                                && response.getBody().getFeatureId().equals(1L))
                .verifyComplete();

        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    public void testGetTaskById_NotFound() {
        when(taskService.getTaskById(1L))
                .thenReturn(Mono.error(new ServerWebInputException("Task not found")));

        StepVerifier.create(taskController.getTaskById(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    public void testCreateTask_Success() {
        Task task = createTask(null, 1L, "New Task", "New Description", 1L);
        Task savedTask = createTask(1L, 1L, "New Task", "New Description", 1L);
        savedTask.setCreateDate(LocalDateTime.now());
        savedTask.setUpdateDate(LocalDateTime.now());

        when(taskService.createTask(any(Task.class))).thenReturn(Mono.just(savedTask));

        StepVerifier.create(taskController.createTask(task))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getId().equals(1L)
                                && response.getBody().getCreateDate() != null
                                && response.getBody().getUpdateDate() != null
                                && response.getBody().equals(savedTask))
                .verifyComplete();

        verify(taskService, times(1)).createTask(any(Task.class));
    }

    @Test
    public void testCreateTask_Error() {
        Task task = createTask(null, 1L, "Task", "Description", 1L);

        when(taskService.createTask(any(Task.class)))
                .thenReturn(Mono.error(new ServerWebInputException("Invalid input")));

        StepVerifier.create(taskController.createTask(task))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(taskService, times(1)).createTask(any(Task.class));
    }

    @Test
    public void testUpdateTask_Success() {
        Task task = createTask(1L, 1L, "Updated Task", "Updated Description", 1L);
        task.setCreateDate(LocalDateTime.now().minusDays(5));
        task.setUpdateDate(LocalDateTime.now());

        when(taskService.updateTask(anyLong(), any(Task.class))).thenReturn(Mono.just(task));

        StepVerifier.create(taskController.updateTask(1L, task))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getId().equals(1L))
                .verifyComplete();

        verify(taskService, times(1)).updateTask(eq(1L), any(Task.class));
    }

    @Test
    public void testUpdateTask_NotFound() {
        Task task = createTask(1L, 1L, "Task", "Description", 1L);

        when(taskService.updateTask(anyLong(), any(Task.class)))
                .thenReturn(Mono.error(new ServerWebInputException("Task not found")));

        StepVerifier.create(taskController.updateTask(1L, task))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(taskService, times(1)).updateTask(eq(1L), any(Task.class));
    }

    @Test
    public void testUpdateTask_Error() {
        Task task = createTask(1L, 1L, "Task", "Description", 1L);

        when(taskService.updateTask(anyLong(), any(Task.class)))
                .thenReturn(Mono.error(new ServerWebInputException("Invalid input")));

        StepVerifier.create(taskController.updateTask(1L, task))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(taskService, times(1)).updateTask(eq(1L), any(Task.class));
    }

    @Test
    public void testDeleteTask_Success() {
        when(taskService.deleteTask(1L)).thenReturn(Mono.empty());

        StepVerifier.create(taskController.deleteTask(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NO_CONTENT)
                .verifyComplete();

        verify(taskService, times(1)).deleteTask(1L);
    }

    @Test
    public void testDeleteTask_NotFound() {
        when(taskService.deleteTask(1L))
                .thenReturn(Mono.error(new ServerWebInputException("Task not found")));

        StepVerifier.create(taskController.deleteTask(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(taskService, times(1)).deleteTask(1L);
    }

    @Test
    public void testCreateTask_ReturnsTaskWithDates() {
        Task task = createTask(null, 1L, "New Task", "Description", 1L);
        Task savedTask = createTask(1L, 1L, "New Task", "Description", 1L);
        savedTask.setCreateDate(LocalDateTime.now());
        savedTask.setUpdateDate(LocalDateTime.now());

        when(taskService.createTask(any(Task.class))).thenReturn(Mono.just(savedTask));

        StepVerifier.create(taskController.createTask(task))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getId().equals(1L)
                                && response.getBody().getCreateDate() != null
                                && response.getBody().getUpdateDate() != null)
                .verifyComplete();

        verify(taskService, times(1)).createTask(any(Task.class));
    }

    @Test
    public void testUpdateTask_ReturnsTaskWithUpdatedDate() {
        LocalDateTime originalCreateDate = LocalDateTime.now().minusDays(5);
        LocalDateTime originalUpdateDate = LocalDateTime.now().minusDays(2);
        Task existingTask = createTask(1L, 1L, "Old Task", "Description", 1L);
        existingTask.setCreateDate(originalCreateDate);
        existingTask.setUpdateDate(originalUpdateDate);

        Task updatedTask = createTask(1L, 1L, "Updated Task", "Description", 1L);
        updatedTask.setCreateDate(originalCreateDate);
        updatedTask.setUpdateDate(LocalDateTime.now()); // Simulate update

        when(taskService.updateTask(anyLong(), any(Task.class)))
                .thenReturn(Mono.just(updatedTask));

        StepVerifier.create(taskController.updateTask(1L, existingTask))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getId().equals(1L)
                                && response.getBody().getCreateDate().equals(originalCreateDate)
                                && response.getBody().getUpdateDate().isAfter(originalUpdateDate))
                .verifyComplete();

        verify(taskService, times(1)).updateTask(eq(1L), any(Task.class));
    }

    @Test
    public void testGetTaskById_ReturnsTaskWithDates() {
        LocalDateTime createDate = LocalDateTime.now().minusDays(10);
        LocalDateTime updateDate = LocalDateTime.now().minusDays(5);
        Task task = createTask(1L, 1L, "Test Task", "Description", 1L);
        task.setCreateDate(createDate);
        task.setUpdateDate(updateDate);

        when(taskService.getTaskById(1L)).thenReturn(Mono.just(task));

        StepVerifier.create(taskController.getTaskById(1L))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.OK
                                && response.getBody() != null
                                && response.getBody().getId().equals(1L)
                                && response.getBody().getCreateDate().equals(createDate)
                                && response.getBody().getUpdateDate().equals(updateDate))
                .verifyComplete();

        verify(taskService, times(1)).getTaskById(1L);
    }

    private Task createTask(Long id, Long featureId, String summary, String description, Long authorId) {
        return Task.builder()
                .id(id)
                .featureId(featureId)
                .summary(summary)
                .description(description)
                .authorId(authorId)
                .build();
    }
}
