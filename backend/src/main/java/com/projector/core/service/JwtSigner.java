package com.projector.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projector.core.model.UserClaims;
import com.projector.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtSigner {

    private static final ConcurrentHashMap<String, Jws<Claims>> SIGNATURE_CACHE = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final KeyPair keyPair;

    @Value("${jwt.token.max-age:3600}")
    private long maxAge;

    public JwtSigner() throws NoSuchAlgorithmException {
        // Генерируем RSA ключи для RS256 алгоритма
        // В production версии ключи должны храниться в конфигурации или секретах
        this.keyPair = Keys.keyPairFor(io.jsonwebtoken.SignatureAlgorithm.RS256);
        log.info("JWT key pair generated successfully");
    }

    public String createUserJwt(User user, List<String> authorities) {
        UserClaims userClaims = new UserClaims(User.forCookie(user), authorities);
        String subject;
        try {
            subject = objectMapper
                    .writerFor(UserClaims.class)
                    .writeValueAsString(userClaims);
        } catch (JsonProcessingException e) {
            log.error("Incorrect JSON auth {}", e.getMessage());
            throw new RuntimeException(e);
        }

        Date expirationDate = Date.from(Instant.now().plus(Duration.ofSeconds(maxAge)));

        return Jwts.builder()
                .signWith(keyPair.getPrivate(), io.jsonwebtoken.SignatureAlgorithm.RS256)
                .setSubject(subject)
                .setIssuer("projector")
                .setExpiration(expirationDate)
                .setIssuedAt(Date.from(Instant.now()))
                .compact();
    }

    public Jws<Claims> validateJwt(String jwt) {
        return SIGNATURE_CACHE.computeIfAbsent(jwt, s -> {
            try {
                return Jwts.parser()
                        .verifyWith(keyPair.getPublic())
                        .build()
                        .parseSignedClaims(s);
            } catch (Exception e) {
                throw new RuntimeException("Failed to validate JWT", e);
            }
        });
    }
}

