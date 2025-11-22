package com.projector.core.component;

import com.projector.core.config.Constants;
import com.projector.core.exception.InvalidTokenException;
import com.projector.core.service.JwtSigner;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    private final JwtSigner jwtSigner;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        if (exchange != null
                && exchange.getRequest().getCookies().get(Constants.AUTH_COOKIE_NAME) != null
                && !exchange.getRequest().getCookies().get(Constants.AUTH_COOKIE_NAME).isEmpty()) {
            String token = exchange.getRequest().getCookies().get(Constants.AUTH_COOKIE_NAME).get(0).getValue();

            try {
                jwtSigner.validateJwt(token);
                return Mono.just(new UsernamePasswordAuthenticationToken(token, token));
            } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException |
                     IllegalArgumentException | DecodingException ex) {
                log.warn("Token is incorrect: {}", ex.getMessage());
                exchange.getResponse().setStatusCode(InvalidTokenException.STATUS);
                exchange.getResponse().getHeaders()
                        .set("Set-Cookie", ResponseCookie.from(Constants.AUTH_COOKIE_NAME, "deleted")
                                .path("/")
                                .maxAge(0)
                                .build().toString());

                return Mono.error(new InvalidTokenException("Невалидный токен"));
            }
        } else {
            return Mono.empty();
        }
    }
}

