package com.projector.role.controller;

import com.projector.role.model.Role;
import com.projector.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping(value = "/api/roles", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoleController {

    private final RoleService roleService;

    @Operation(
            summary = "Get all roles",
            description = "Retrieve a list of all roles with their authorities",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "List of roles",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        array =
                                                @ArraySchema(
                                                        schema =
                                                                @Schema(
                                                                        implementation =
                                                                                Role.class))))
            })
    @GetMapping
    public Flux<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    @Operation(
            summary = "Get role by ID",
            description = "Retrieve a specific role by its ID",
            parameters = {
                @Parameter(
                        in = ParameterIn.PATH,
                        name = "id",
                        required = true,
                        description = "Role ID",
                        schema = @Schema(type = "integer", format = "int64", example = "1"))
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Role found",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = Role.class))),
                @ApiResponse(responseCode = "404", description = "Role not found")
            })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Role>> getRoleById(@PathVariable Long id) {
        return roleService
                .getRoleById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(
            summary = "Create a new role",
            description = "Create a new role with specified name and authorities",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Role created",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = Role.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid input or role name already exists")
            })
    @PostMapping
    public Mono<ResponseEntity<Role>> createRole(@RequestBody Role role) {
        return roleService
                .createRole(role)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @Operation(
            summary = "Update an existing role",
            description = "Update role information by ID",
            parameters = {
                @Parameter(
                        in = ParameterIn.PATH,
                        name = "id",
                        required = true,
                        description = "Role ID",
                        schema = @Schema(type = "integer", format = "int64", example = "1"))
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Role updated",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = Role.class))),
                @ApiResponse(responseCode = "404", description = "Role not found"),
                @ApiResponse(responseCode = "400", description = "Invalid input")
            })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Role>> updateRole(@PathVariable Long id, @RequestBody Role role) {
        return roleService
                .updateRole(id, role)
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
            summary = "Delete a role",
            description = "Delete a role by ID",
            parameters = {
                @Parameter(
                        in = ParameterIn.PATH,
                        name = "id",
                        required = true,
                        description = "Role ID",
                        schema = @Schema(type = "integer", format = "int64", example = "1"))
            },
            responses = {
                @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
                @ApiResponse(responseCode = "404", description = "Role not found")
            })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteRole(@PathVariable Long id) {
        return roleService
                .deleteRole(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(
            summary = "Update role authorities",
            description = "Update authorities for a specific role",
            parameters = {
                @Parameter(
                        in = ParameterIn.PATH,
                        name = "id",
                        required = true,
                        description = "Role ID",
                        schema = @Schema(type = "integer", format = "int64", example = "1"))
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Authorities updated",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = Role.class))),
                @ApiResponse(responseCode = "404", description = "Role not found"),
                @ApiResponse(responseCode = "400", description = "Invalid authorities")
            })
    @PostMapping("/{id}/authorities")
    public Mono<ResponseEntity<Role>> updateAuthorities(
            @PathVariable Long id, @RequestBody Set<String> authorities) {
        return roleService
                .updateAuthorities(id, authorities)
                .map(ResponseEntity::ok)
                .onErrorResume(
                        error -> {
                            if (error.getMessage().contains("not found")) {
                                return Mono.just(ResponseEntity.notFound().build());
                            }
                            return Mono.just(ResponseEntity.badRequest().build());
                        });
    }
}
