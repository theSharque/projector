package com.projector.core.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import com.projector.core.config.Constants;
import com.projector.core.exception.InvalidTokenException;
import com.projector.core.model.UserCredentials;
import com.projector.role.model.Role;
import com.projector.role.repository.RoleRepository;
import com.projector.user.model.User;
import com.projector.user.service.UserService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtSigner jwtSigner;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(authService, "maxAge", 3600L);
    }

    @Test
    public void testLogin_Success() {
        User user = User.builder().id(1L).email("admin").passHash("hash").build();
        UserCredentials credentials = new UserCredentials("admin", "admin");
        String jwtToken = "test-jwt-token";
        Role role = createRole(1L, "Admin", Set.of("USER_VIEW", "USER_EDIT"));

        when(userService.getUser("admin", "admin")).thenReturn(Mono.just(user));
        when(roleRepository.findByUserId(1L)).thenReturn(Flux.just(role));
        when(jwtSigner.createUserJwt(any(User.class), anyList())).thenReturn(jwtToken);

        StepVerifier.create(authService.login(credentials))
                .expectNextMatches(cookie -> {
                    return cookie.getName().equals(Constants.AUTH_COOKIE_NAME)
                            && cookie.getValue().equals(jwtToken)
                            && cookie.getMaxAge().getSeconds() == 3600
                            && "/".equals(cookie.getPath());
                })
                .verifyComplete();

        verify(userService, times(1)).getUser("admin", "admin");
        verify(roleRepository, times(1)).findByUserId(1L);
        verify(jwtSigner, times(1)).createUserJwt(any(User.class), anyList());
    }

    @Test
    public void testLogin_Success_NoRoles() {
        User user = User.builder().id(1L).email("admin").passHash("hash").build();
        UserCredentials credentials = new UserCredentials("admin", "admin");
        String jwtToken = "test-jwt-token";

        when(userService.getUser("admin", "admin")).thenReturn(Mono.just(user));
        when(roleRepository.findByUserId(1L)).thenReturn(Flux.empty());
        when(jwtSigner.createUserJwt(any(User.class), eq(Collections.emptyList())))
                .thenReturn(jwtToken);

        StepVerifier.create(authService.login(credentials))
                .expectNextMatches(cookie -> cookie.getValue().equals(jwtToken))
                .verifyComplete();

        verify(userService, times(1)).getUser("admin", "admin");
        verify(roleRepository, times(1)).findByUserId(1L);
        verify(jwtSigner, times(1)).createUserJwt(any(User.class), eq(Collections.emptyList()));
    }

    @Test
    public void testLogin_Success_MultipleRoles() {
        User user = User.builder().id(1L).email("admin").passHash("hash").build();
        UserCredentials credentials = new UserCredentials("admin", "admin");
        String jwtToken = "test-jwt-token";
        Role role1 = createRole(1L, "Role1", Set.of("USER_VIEW"));
        Role role2 = createRole(2L, "Role2", Set.of("ROLE_VIEW", "ROLE_EDIT"));

        when(userService.getUser("admin", "admin")).thenReturn(Mono.just(user));
        when(roleRepository.findByUserId(1L)).thenReturn(Flux.just(role1, role2));
        when(jwtSigner.createUserJwt(any(User.class), anyList())).thenReturn(jwtToken);

        StepVerifier.create(authService.login(credentials))
                .expectNextMatches(cookie -> cookie.getValue().equals(jwtToken))
                .verifyComplete();

        verify(userService, times(1)).getUser("admin", "admin");
        verify(roleRepository, times(1)).findByUserId(1L);
        verify(jwtSigner, times(1)).createUserJwt(any(User.class), anyList());
    }

    @Test
    public void testLogin_InvalidCredentials() {
        UserCredentials credentials = new UserCredentials("admin", "wrongpassword");

        when(userService.getUser("admin", "wrongpassword"))
                .thenReturn(Mono.error(new UsernameNotFoundException("Invalid username or password")));

        StepVerifier.create(authService.login(credentials))
                .expectErrorMatches(
                        throwable -> throwable instanceof InvalidTokenException
                                && throwable.getMessage() != null
                                && throwable.getMessage().contains("Invalid username or password"))
                .verify();

        verify(userService, times(1)).getUser("admin", "wrongpassword");
        verify(roleRepository, never()).findByUserId(anyLong());
        verify(jwtSigner, never()).createUserJwt(any(User.class), anyList());
    }

    @Test
    public void testLogin_UserNotFound() {
        UserCredentials credentials = new UserCredentials("nonexistent", "password");

        when(userService.getUser("nonexistent", "password"))
                .thenReturn(Mono.error(new UsernameNotFoundException("Invalid username or password")));

        StepVerifier.create(authService.login(credentials))
                .expectErrorMatches(
                        throwable -> throwable instanceof InvalidTokenException
                                && throwable.getMessage() != null
                                && throwable.getMessage().contains("Invalid username or password"))
                .verify();

        verify(userService, times(1)).getUser("nonexistent", "password");
        verify(roleRepository, never()).findByUserId(anyLong());
        verify(jwtSigner, never()).createUserJwt(any(User.class), anyList());
    }

    @Test
    public void testLogin_EmptyResult() {
        UserCredentials credentials = new UserCredentials("admin", "admin");

        when(userService.getUser("admin", "admin")).thenReturn(Mono.empty());

        StepVerifier.create(authService.login(credentials))
                .expectErrorMatches(
                        throwable -> throwable instanceof InvalidTokenException
                                && throwable.getMessage() != null
                                && throwable.getMessage().contains("Invalid username or password"))
                .verify();

        verify(userService, times(1)).getUser("admin", "admin");
        verify(roleRepository, never()).findByUserId(anyLong());
        verify(jwtSigner, never()).createUserJwt(any(User.class), anyList());
    }

    @Test
    public void testLogin_InternalError() {
        UserCredentials credentials = new UserCredentials("admin", "admin");

        when(userService.getUser("admin", "admin"))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(authService.login(credentials))
                .expectErrorMatches(
                        throwable -> throwable instanceof InvalidTokenException
                                && throwable.getMessage() != null
                                && throwable.getMessage().contains("Internal authentication error"))
                .verify();

        verify(userService, times(1)).getUser("admin", "admin");
        verify(roleRepository, never()).findByUserId(anyLong());
        verify(jwtSigner, never()).createUserJwt(any(User.class), anyList());
    }

    @Test
    public void testGetCurrentUserAuthorities_Success() {
        Set<String> expectedAuthorities = Set.of("USER_VIEW", "ROLE_VIEW", "TASK_EDIT");
        Authentication authentication = createAuthentication(expectedAuthorities);
        SecurityContext securityContext = createSecurityContext(authentication);

        StepVerifier.create(
                authService.getCurrentUserAuthorities()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(securityContext))))
                .expectNextMatches(authorities -> {
                    return authorities.size() == 3
                            && authorities.contains("USER_VIEW")
                            && authorities.contains("ROLE_VIEW")
                            && authorities.contains("TASK_EDIT");
                })
                .verifyComplete();
    }

    @Test
    public void testGetCurrentUserAuthorities_EmptyAuthorities() {
        Authentication authentication = createAuthentication(Set.of());
        SecurityContext securityContext = createSecurityContext(authentication);

        StepVerifier.create(
                authService.getCurrentUserAuthorities()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(securityContext))))
                .expectNextMatches(authorities -> authorities.isEmpty())
                .verifyComplete();
    }

    @Test
    public void testGetCurrentUserAuthorities_NotAuthenticated() {
        StepVerifier.create(authService.getCurrentUserAuthorities())
                .expectErrorMatches(
                        throwable -> throwable instanceof InvalidTokenException
                                && throwable.getMessage() != null
                                && throwable.getMessage().contains("User not authenticated"))
                .verify();
    }

    private Role createRole(Long id, String name, Set<String> authorities) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setAuthorities(authorities);
        return role;
    }

    private Authentication createAuthentication(Set<String> authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities = Flux.fromIterable(authorities)
                .map(SimpleGrantedAuthority::new)
                .collectList()
                .block();
        return new UsernamePasswordAuthenticationToken(
                "user", "credentials", grantedAuthorities);
    }

    private SecurityContext createSecurityContext(Authentication authentication) {
        return new SecurityContext() {
            @Override
            public Authentication getAuthentication() {
                return authentication;
            }

            @Override
            public void setAuthentication(Authentication authentication) {
                // Not needed for tests
            }
        };
    }
}
