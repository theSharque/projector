package com.projector.core.controller;

import com.projector.core.config.Constants;
import com.projector.core.exception.InvalidTokenException;
import com.projector.core.model.UserCredentials;
import com.projector.core.service.JwtSigner;
import com.projector.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private static final String INVALID_USERNAME_OR_PASSWORD = "Invalid username or password";

    private final UserService userService;
    private final JwtSigner jwtSigner;

    @Value("${jwt.token.max-age:3600}")
    private long maxAge;

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Generate JWT token and return it as a set-cookie header",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserCredentials.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Successful login and generated JWT with user returned",
                            headers = @Header(
                                    name = "Set-Cookie",
                                    description = "Cookie with JWT token",
                                    schema = @Schema(implementation = String.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not found or credentials are incorrect"
                    )
            }
    )
    public Mono<ResponseEntity<Object>> login(@RequestBody UserCredentials userCredentials) {
        return userService.getUser(userCredentials.getEmail(), userCredentials.getPassword())
                .map(user -> {
                    List<String> authorities = Collections.emptyList();
                    String jwt = jwtSigner.createUserJwt(user, authorities);
                    
                    ResponseCookie cookie = ResponseCookie.fromClientResponse(Constants.AUTH_COOKIE_NAME, jwt)
                            .maxAge(maxAge)
                            .path("/")
                            .build();
                    
                    return ResponseEntity.noContent()
                            .header("Set-Cookie", cookie.toString())
                            .build();
                })
                .onErrorResume(throwable -> {
                    if (throwable instanceof UsernameNotFoundException) {
                        return Mono.error(new InvalidTokenException(INVALID_USERNAME_OR_PASSWORD));
                    } else {
                        return Mono.error(new InvalidTokenException(INVALID_USERNAME_OR_PASSWORD));
                    }
                })
                .switchIfEmpty(Mono.error(new InvalidTokenException(INVALID_USERNAME_OR_PASSWORD)));
    }
}

