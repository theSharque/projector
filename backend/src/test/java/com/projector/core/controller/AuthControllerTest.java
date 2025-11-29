package com.projector.core.controller;

import com.projector.core.exception.InvalidTokenException;
import com.projector.core.model.UserCredentials;
import com.projector.core.service.JwtSigner;
import com.projector.user.model.User;
import com.projector.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtSigner jwtSigner;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(authController, "maxAge", 3600L);
    }

    @Test
    public void testLogin_Success() {
        User user = User.builder().id(1L).email("admin").passHash("hash").build();
        UserCredentials credentials = new UserCredentials("admin", "admin");
        String jwtToken = "test-jwt-token";

        when(userService.getUser("admin", "admin")).thenReturn(Mono.just(user));
        when(jwtSigner.createUserJwt(any(User.class), anyList())).thenReturn(jwtToken);

        StepVerifier.create(authController.login(credentials))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NO_CONTENT
                        && response.getHeaders().containsKey("Set-Cookie"))
                .verifyComplete();

        verify(userService, times(1)).getUser("admin", "admin");
        verify(jwtSigner, times(1)).createUserJwt(any(User.class), eq(Collections.emptyList()));
    }

    @Test
    public void testLogin_InvalidCredentials() {
        UserCredentials credentials = new UserCredentials("admin", "wrongpassword");

        when(userService.getUser("admin", "wrongpassword"))
                .thenReturn(Mono.error(new UsernameNotFoundException("Invalid username or password")));

        StepVerifier.create(authController.login(credentials))
                .expectErrorMatches(throwable -> throwable instanceof InvalidTokenException
                        && throwable.getMessage() != null
                        && throwable.getMessage().contains("Invalid username or password"))
                .verify();

        verify(userService, times(1)).getUser("admin", "wrongpassword");
        verify(jwtSigner, never()).createUserJwt(any(User.class), anyList());
    }

    @Test
    public void testLogin_UserNotFound() {
        UserCredentials credentials = new UserCredentials("nonexistent", "password");

        when(userService.getUser("nonexistent", "password"))
                .thenReturn(Mono.error(new UsernameNotFoundException("Invalid username or password")));

        StepVerifier.create(authController.login(credentials))
                .expectErrorMatches(throwable -> throwable instanceof InvalidTokenException
                        && throwable.getMessage() != null
                        && throwable.getMessage().contains("Invalid username or password"))
                .verify();

        verify(userService, times(1)).getUser("nonexistent", "password");
        verify(jwtSigner, never()).createUserJwt(any(User.class), anyList());
    }

    @Test
    public void testLogin_EmptyResult() {
        UserCredentials credentials = new UserCredentials("admin", "admin");

        when(userService.getUser("admin", "admin")).thenReturn(Mono.empty());

        StepVerifier.create(authController.login(credentials))
                .expectErrorMatches(throwable -> throwable instanceof InvalidTokenException
                        && throwable.getMessage() != null
                        && throwable.getMessage().contains("Invalid username or password"))
                .verify();

        verify(userService, times(1)).getUser("admin", "admin");
        verify(jwtSigner, never()).createUserJwt(any(User.class), anyList());
    }

    @Test
    public void testLogin_CookieHeaderSet() {
        User user = User.builder().id(1L).email("admin").passHash("hash").build();
        UserCredentials credentials = new UserCredentials("admin", "admin");
        String jwtToken = "test-jwt-token";

        when(userService.getUser("admin", "admin")).thenReturn(Mono.just(user));
        when(jwtSigner.createUserJwt(any(User.class), anyList())).thenReturn(jwtToken);

        StepVerifier.create(authController.login(credentials))
                .expectNextMatches(response -> {
                    String setCookie = response.getHeaders().getFirst("Set-Cookie");
                    return setCookie != null
                            && setCookie.contains("X-Auth=" + jwtToken)
                            && setCookie.contains("Path=/")
                            && setCookie.contains("Max-Age=3600");
                })
                .verifyComplete();

        verify(userService, times(1)).getUser("admin", "admin");
        verify(jwtSigner, times(1)).createUserJwt(any(User.class), eq(Collections.emptyList()));
    }
}

