package com.projector.user.controller;

import com.projector.user.model.User;
import com.projector.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllUsers_Success() {
        User user1 = User.builder().id(1L).email("user1@test.com").passHash("hash1").build();
        User user2 = User.builder().id(2L).email("user2@test.com").passHash("hash2").build();

        when(userService.getAllUsers()).thenReturn(Flux.just(user1, user2));

        StepVerifier.create(userController.getAllUsers())
                .expectNext(user1)
                .expectNext(user2)
                .verifyComplete();

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    public void testGetUserById_Success() {
        User user = User.builder().id(1L).email("test@test.com").passHash("hash").build();

        when(userService.getUserById(1L)).thenReturn(Mono.just(user));

        StepVerifier.create(userController.getUserById(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK
                        && response.getBody().equals(user))
                .verifyComplete();

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userService.getUserById(1L)).thenReturn(Mono.error(new ServerWebInputException("User not found")));

        StepVerifier.create(userController.getUserById(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    public void testGetUserByEmail_Success() {
        User user = User.builder().id(1L).email("test@test.com").passHash("hash").build();

        when(userService.getUserByEmail("test@test.com")).thenReturn(Mono.just(user));

        StepVerifier.create(userController.getUserByEmail("test@test.com"))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK
                        && response.getBody().equals(user))
                .verifyComplete();

        verify(userService, times(1)).getUserByEmail("test@test.com");
    }

    @Test
    public void testGetUserByEmail_NotFound() {
        when(userService.getUserByEmail("test@test.com")).thenReturn(Mono.error(new ServerWebInputException("User not found")));

        StepVerifier.create(userController.getUserByEmail("test@test.com"))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(userService, times(1)).getUserByEmail("test@test.com");
    }

    @Test
    public void testCreateUser_Success() {
        User user = User.builder().email("test@test.com").passHash("hash").build();
        User savedUser = User.builder().id(1L).email("test@test.com").passHash("hash").build();

        when(userService.createUser(any(User.class))).thenReturn(Mono.just(savedUser));

        StepVerifier.create(userController.createUser(user))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK
                        && response.getBody().equals(savedUser))
                .verifyComplete();

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    public void testCreateUser_Error() {
        User user = User.builder().email("test@test.com").passHash("hash").build();

        when(userService.createUser(any(User.class))).thenReturn(Mono.error(new ServerWebInputException("Email already exists")));

        StepVerifier.create(userController.createUser(user))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    public void testUpdateUser_Success() {
        User user = User.builder().id(1L).email("test@test.com").passHash("newhash").build();

        when(userService.updateUser(anyLong(), any(User.class))).thenReturn(Mono.just(user));

        StepVerifier.create(userController.updateUser(1L, user))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK
                        && response.getBody().equals(user))
                .verifyComplete();

        verify(userService, times(1)).updateUser(eq(1L), any(User.class));
    }

    @Test
    public void testUpdateUser_Error() {
        User user = User.builder().id(1L).email("test@test.com").passHash("hash").build();

        when(userService.updateUser(anyLong(), any(User.class))).thenReturn(Mono.error(new ServerWebInputException("User not found")));

        StepVerifier.create(userController.updateUser(1L, user))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(userService, times(1)).updateUser(eq(1L), any(User.class));
    }

    @Test
    public void testDeleteUser_Success() {
        when(userService.deleteUser(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userController.deleteUser(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NO_CONTENT)
                .verifyComplete();

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    public void testDeleteUser_Error() {
        when(userService.deleteUser(1L)).thenReturn(Mono.error(new ServerWebInputException("User not found")));

        StepVerifier.create(userController.deleteUser(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(userService, times(1)).deleteUser(1L);
    }
}

