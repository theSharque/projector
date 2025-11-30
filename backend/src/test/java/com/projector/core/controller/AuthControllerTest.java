package com.projector.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;

import com.projector.core.exception.InvalidTokenException;
import com.projector.core.model.UserCredentials;
import com.projector.core.service.AuthService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLogin_Success() {
        UserCredentials credentials = new UserCredentials("admin", "admin");
        String jwtToken = "test-jwt-token";
        ResponseCookie cookie = ResponseCookie.from("X-Auth", jwtToken)
                .maxAge(3600)
                .path("/")
                .build();

        when(authService.login(any(UserCredentials.class))).thenReturn(Mono.just(cookie));

        StepVerifier.create(authController.login(credentials))
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.NO_CONTENT
                                && response.getHeaders().containsKey("Set-Cookie"))
                .verifyComplete();

        verify(authService, times(1)).login(any(UserCredentials.class));
    }

    @Test
    public void testLogin_InvalidCredentials() {
        UserCredentials credentials = new UserCredentials("admin", "wrongpassword");

        when(authService.login(any(UserCredentials.class)))
                .thenReturn(Mono.error(new InvalidTokenException("Invalid username or password")));

        StepVerifier.create(authController.login(credentials))
                .expectErrorMatches(
                        throwable -> throwable instanceof InvalidTokenException
                                && throwable.getMessage() != null
                                && throwable
                                        .getMessage()
                                        .contains("Invalid username or password"))
                .verify();

        verify(authService, times(1)).login(any(UserCredentials.class));
    }

    @Test
    public void testLogin_UserNotFound() {
        UserCredentials credentials = new UserCredentials("nonexistent", "password");

        when(authService.login(any(UserCredentials.class)))
                .thenReturn(Mono.error(new InvalidTokenException("Invalid username or password")));

        StepVerifier.create(authController.login(credentials))
                .expectErrorMatches(
                        throwable -> throwable instanceof InvalidTokenException
                                && throwable.getMessage() != null
                                && throwable
                                        .getMessage()
                                        .contains("Invalid username or password"))
                .verify();

        verify(authService, times(1)).login(any(UserCredentials.class));
    }

    @Test
    public void testLogin_EmptyResult() {
        UserCredentials credentials = new UserCredentials("admin", "admin");

        when(authService.login(any(UserCredentials.class)))
                .thenReturn(Mono.error(new InvalidTokenException("Invalid username or password")));

        StepVerifier.create(authController.login(credentials))
                .expectErrorMatches(
                        throwable -> throwable instanceof InvalidTokenException
                                && throwable.getMessage() != null
                                && throwable
                                        .getMessage()
                                        .contains("Invalid username or password"))
                .verify();

        verify(authService, times(1)).login(any(UserCredentials.class));
    }

    @Test
    public void testLogin_CookieHeaderSet() {
        UserCredentials credentials = new UserCredentials("admin", "admin");
        String jwtToken = "test-jwt-token";
        ResponseCookie cookie = ResponseCookie.from("X-Auth", jwtToken)
                .maxAge(3600)
                .path("/")
                .build();

        when(authService.login(any(UserCredentials.class))).thenReturn(Mono.just(cookie));

        StepVerifier.create(authController.login(credentials))
                .expectNextMatches(
                        response -> {
                            String setCookie = response.getHeaders().getFirst("Set-Cookie");
                            return setCookie != null
                                    && setCookie.contains("X-Auth=" + jwtToken)
                                    && setCookie.contains("Path=/")
                                    && setCookie.contains("Max-Age=3600");
                        })
                .verifyComplete();

        verify(authService, times(1)).login(any(UserCredentials.class));
    }
}
