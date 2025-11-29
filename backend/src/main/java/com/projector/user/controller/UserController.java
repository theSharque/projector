package com.projector.user.controller;

import com.projector.user.model.User;
import com.projector.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ApiResponses({
    @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
})
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Get all users",
            description = "Retrieve a list of all active users",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "List of users",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        array =
                                                @ArraySchema(
                                                        schema =
                                                                @Schema(
                                                                        implementation =
                                                                                User.class))))
            })
    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public Flux<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieve a specific user by its ID",
            parameters = {
                @Parameter(
                        in = ParameterIn.PATH,
                        name = "id",
                        required = true,
                        description = "User ID",
                        schema = @Schema(type = "integer", format = "int64", example = "1"))
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User found",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = User.class))),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public Mono<ResponseEntity<User>> getUserById(@PathVariable Long id) {
        return userService
                .getUserById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(
            summary = "Get user by email",
            description = "Retrieve a specific user by email address",
            parameters = {
                @Parameter(
                        in = ParameterIn.PATH,
                        name = "email",
                        required = true,
                        description = "User email",
                        schema = @Schema(type = "string", example = "user@example.com"))
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User found",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = User.class))),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public Mono<ResponseEntity<User>> getUserByEmail(@PathVariable String email) {
        return userService
                .getUserByEmail(email)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(
            summary = "Create a new user",
            description = "Create a new user with specified email and password",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User created",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = User.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid input or user email already exists")
            })
    @PostMapping
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public Mono<ResponseEntity<User>> createUser(@RequestBody User user) {
        return userService
                .createUser(user)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @Operation(
            summary = "Update an existing user",
            description = "Update user information by ID",
            parameters = {
                @Parameter(
                        in = ParameterIn.PATH,
                        name = "id",
                        required = true,
                        description = "User ID",
                        schema = @Schema(type = "integer", format = "int64", example = "1"))
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User updated",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = User.class))),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "400", description = "Invalid input")
            })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public Mono<ResponseEntity<User>> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService
                .updateUser(id, user)
                .map(ResponseEntity::ok)
                .onErrorResume(
                        error -> {
                            if (error.getMessage().contains("not found")) {
                                return Mono.just(ResponseEntity.notFound().build());
                            }
                            return Mono.just(ResponseEntity.badRequest().build());
                        });
    }

    @Operation(
            summary = "Delete a user",
            description = "Delete a user by ID from database",
            parameters = {
                @Parameter(
                        in = ParameterIn.PATH,
                        name = "id",
                        required = true,
                        description = "User ID",
                        schema = @Schema(type = "integer", format = "int64", example = "1"))
            },
            responses = {
                @ApiResponse(responseCode = "204", description = "User deleted successfully"),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable Long id) {
        return userService
                .deleteUser(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }
}
