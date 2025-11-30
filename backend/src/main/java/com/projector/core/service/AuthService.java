package com.projector.core.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.projector.core.config.Constants;
import com.projector.core.exception.InvalidTokenException;
import com.projector.core.model.UserCredentials;
import com.projector.role.repository.RoleRepository;
import com.projector.user.model.User;
import com.projector.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String INVALID_USERNAME_OR_PASSWORD = "Invalid username or password";
    private static final String INTERNAL_AUTH_ERROR = "Internal authentication error";

    private final UserService userService;
    private final JwtSigner jwtSigner;
    private final RoleRepository roleRepository;

    @Value("${jwt.token.max-age:3600}")
    private long maxAge;

    public Mono<ResponseCookie> login(UserCredentials userCredentials) {
        return userService
                .getUser(userCredentials.getEmail(), userCredentials.getPassword())
                .flatMap(user -> getUserAuthorities(user)
                        .map(authorities -> {
                            String jwt = jwtSigner.createUserJwt(user, authorities);
                            return createAuthCookie(jwt);
                        }))
                .onErrorResume(
                        throwable -> {
                            if (throwable instanceof UsernameNotFoundException) {
                                return Mono.error(
                                        new InvalidTokenException(INVALID_USERNAME_OR_PASSWORD));
                            } else {
                                return Mono.error(new InvalidTokenException(INTERNAL_AUTH_ERROR));
                            }
                        })
                .switchIfEmpty(Mono.error(new InvalidTokenException(INVALID_USERNAME_OR_PASSWORD)));
    }

    public Mono<Set<String>> getCurrentUserAuthorities() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(Authentication.class)
                .flatMap(authentication -> Flux.fromIterable(authentication.getAuthorities())
                        .map(GrantedAuthority::getAuthority)
                        .collectList()
                        .map(Set::copyOf))
                .switchIfEmpty(Mono.error(new InvalidTokenException("User not authenticated")));
    }

    private Mono<List<String>> getUserAuthorities(User user) {
        return roleRepository
                .findByUserId(user.getId())
                .flatMap(role -> Flux.fromIterable(role.getAuthorities()))
                .distinct()
                .sort()
                .collectList()
                .defaultIfEmpty(List.of());
    }

    private ResponseCookie createAuthCookie(String jwt) {
        return ResponseCookie.fromClientResponse(Constants.AUTH_COOKIE_NAME, jwt)
                .maxAge(maxAge)
                .path("/")
                .build();
    }
}
