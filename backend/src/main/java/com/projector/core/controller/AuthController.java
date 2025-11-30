package com.projector.core.controller;

import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projector.core.model.UserCredentials;
import com.projector.core.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Generate JWT token and return it as a set-cookie header", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User credentials", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserCredentials.class))), responses = {
            @ApiResponse(responseCode = "204", description = "Successful login and generated JWT with user returned", headers = @Header(name = "Set-Cookie", description = "Cookie with JWT token", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "User not found or credentials are incorrect")
    })
    public Mono<ResponseEntity<Object>> login(@RequestBody UserCredentials userCredentials) {
        return authService
                .login(userCredentials)
                .map(cookie -> ResponseEntity.noContent()
                        .header("Set-Cookie", cookie.toString())
                        .build());
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile", description = "Get set of authorities for the currently authenticated user", responses = {
            @ApiResponse(responseCode = "200", description = "User authorities", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Set.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public Mono<ResponseEntity<Set<String>>> getProfile() {
        return authService
                .getCurrentUserAuthorities()
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.status(401).build()));
    }
}
