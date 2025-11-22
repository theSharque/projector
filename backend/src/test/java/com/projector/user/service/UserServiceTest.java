package com.projector.user.service;

import com.projector.user.model.User;
import com.projector.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllUsers_Success() {
        User user1 = User.builder().id(1L).email("user1@test.com").passHash("hash1").build();
        User user2 = User.builder().id(2L).email("user2@test.com").passHash("hash2").build();

        when(userRepository.findAll()).thenReturn(Flux.just(user1, user2));

        StepVerifier.create(userService.getAllUsers())
                .expectNext(user1)
                .expectNext(user2)
                .verifyComplete();

        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testGetUserById_Success() {
        User user = User.builder().id(1L).email("test@test.com").passHash("hash").build();

        when(userRepository.findById(1L)).thenReturn(Mono.just(user));

        StepVerifier.create(userService.getUserById(1L))
                .expectNextMatches(u -> u.getId().equals(1L) && u.getEmail().equals("test@test.com"))
                .verifyComplete();

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.getUserById(1L))
                .expectErrorMatches(throwable -> {
                    if (throwable instanceof ServerWebInputException) {
                        String message = throwable.getMessage();
                        return message != null && (message.contains("User not found") || message.contains("not found"));
                    }
                    return false;
                })
                .verify();

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetUserByEmail_Success() {
        User user = User.builder().id(1L).email("test@test.com").passHash("hash").build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Mono.just(user));

        StepVerifier.create(userService.getUserByEmail("test@test.com"))
                .expectNextMatches(u -> u.getEmail().equals("test@test.com"))
                .verifyComplete();

        verify(userRepository, times(1)).findByEmail("test@test.com");
    }

    @Test
    public void testGetUserByEmail_NotFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Mono.empty());

        StepVerifier.create(userService.getUserByEmail("test@test.com"))
                .expectErrorMatches(throwable -> throwable instanceof ServerWebInputException
                        && throwable.getMessage().contains("User not found"))
                .verify();

        verify(userRepository, times(1)).findByEmail("test@test.com");
    }

    @Test
    public void testGetUser_Authentication_Success() {
        String passwordHash = "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918"; // SHA256("admin")
        User user = User.builder().id(1L).email("admin").passHash(passwordHash).build();

        when(userRepository.findByEmail("admin")).thenReturn(Mono.just(user));

        StepVerifier.create(userService.getUser("admin", "admin"))
                .expectNextMatches(u -> u.getId().equals(1L) && u.getEmail().equals("admin"))
                .verifyComplete();

        verify(userRepository, times(1)).findByEmail("admin");
    }

    @Test
    public void testGetUser_Authentication_WrongPassword() {
        String passwordHash = "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918"; // SHA256("admin")
        User user = User.builder().id(1L).email("admin").passHash(passwordHash).build();

        when(userRepository.findByEmail("admin")).thenReturn(Mono.just(user));

        StepVerifier.create(userService.getUser("admin", "wrongpassword"))
                .expectErrorMatches(throwable -> throwable instanceof UsernameNotFoundException
                        && throwable.getMessage().equals("Invalid username or password"))
                .verify();

        verify(userRepository, times(1)).findByEmail("admin");
    }

    @Test
    public void testGetUser_Authentication_UserNotFound() {
        when(userRepository.findByEmail("admin")).thenReturn(Mono.empty());

        StepVerifier.create(userService.getUser("admin", "admin"))
                .expectErrorMatches(throwable -> throwable instanceof UsernameNotFoundException
                        && throwable.getMessage().equals("Invalid username or password"))
                .verify();

        verify(userRepository, times(1)).findByEmail("admin");
    }

    @Test
    public void testCreateUser_Success() {
        User user = User.builder().email("test@test.com").passHash("hash").build();
        User savedUser = User.builder().id(1L).email("test@test.com").passHash("hash").build();

        when(userRepository.existsByEmail("test@test.com")).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        StepVerifier.create(userService.createUser(user))
                .expectNextMatches(u -> u.getId().equals(1L) && u.getEmail().equals("test@test.com"))
                .verifyComplete();

        verify(userRepository, times(1)).existsByEmail("test@test.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testCreateUser_EmailAlreadyExists() {
        User user = User.builder().email("test@test.com").passHash("hash").build();

        when(userRepository.existsByEmail("test@test.com")).thenReturn(Mono.just(true));

        StepVerifier.create(userService.createUser(user))
                .expectErrorMatches(throwable -> throwable instanceof ServerWebInputException
                        && throwable.getMessage().contains("already exists"))
                .verify();

        verify(userRepository, times(1)).existsByEmail("test@test.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testCreateUser_InvalidEmail() {
        User user = User.builder().email("invalid-email").passHash("hash").build();

        StepVerifier.create(userService.createUser(user))
                .expectErrorMatches(throwable -> throwable instanceof ServerWebInputException
                        && throwable.getMessage().contains("Invalid email format"))
                .verify();

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testUpdateUser_Success() {
        User existingUser = User.builder().id(1L).email("test@test.com").passHash("oldhash").build();
        User updateData = User.builder().id(1L).email("test@test.com").passHash("newhash").build();
        User updatedUser = User.builder().id(1L).email("test@test.com").passHash("newhash").build();

        when(userRepository.findById(1L)).thenReturn(Mono.just(existingUser));
        when(userRepository.findByEmail("test@test.com")).thenReturn(Mono.just(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));

        StepVerifier.create(userService.updateUser(1L, updateData))
                .expectNextMatches(u -> u.getId().equals(1L) && u.getPassHash().equals("newhash"))
                .verifyComplete();

        verify(userRepository, times(2)).findById(1L);
        verify(userRepository, times(1)).findByEmail("test@test.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testUpdateUser_NotFound() {
        User updateData = User.builder().id(1L).email("test@test.com").passHash("hash").build();

        when(userRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.updateUser(1L, updateData))
                .expectErrorMatches(throwable -> throwable instanceof ServerWebInputException
                        && throwable.getMessage().contains("User not found"))
                .verify();

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testDeleteUser_Success() {
        User user = User.builder().id(1L).email("test@test.com").passHash("hash").build();

        when(userRepository.findById(1L)).thenReturn(Mono.just(user));
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUser(1L))
                .verifyComplete();

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUser(1L))
                .expectErrorMatches(throwable -> throwable instanceof ServerWebInputException
                        && throwable.getMessage().contains("User not found"))
                .verify();

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}

