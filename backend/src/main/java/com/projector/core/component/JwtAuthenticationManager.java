package com.projector.core.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projector.core.exception.InvalidTokenException;
import com.projector.core.model.UserClaims;
import com.projector.user.model.User;
import com.projector.core.service.JwtSigner;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtSigner jwtSigner;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .map(auth -> jwtSigner.validateJwt((String) auth.getCredentials()))
                .onErrorResume(err -> Mono.error(new UsernameNotFoundException("Неверный токен")))
                .flatMap(jws -> {
                    String subject = jws.getPayload().getSubject();

                    try {
                        UserClaims userClaims = objectMapper.readerFor(UserClaims.class).readValue(subject);
                        return authenticateUserToken(userClaims, authentication);
                    } catch (JsonProcessingException e) {
                        log.error("Incorrect Token subject {}", e.getMessage());
                        return Mono.error(new UsernameNotFoundException("Неверный токен"));
                    }
                });
    }

    private Mono<Authentication> authenticateUserToken(UserClaims userClaims, Authentication authentication) {
        List<SimpleGrantedAuthority> authorities = userClaims.getAuthorities() != null
                ? userClaims.getAuthorities().parallelStream()
                        .distinct()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
                : List.of();

        // TODO: Добавить проверку пользователя в БД
        User user = userClaims.getUser();

        return Mono.just((Authentication) new UsernamePasswordAuthenticationToken(
                user,
                authentication.getCredentials(),
                authorities));
    }
}

