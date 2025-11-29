package com.projector.project.controller;

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

import com.projector.project.model.Project;
import com.projector.project.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
})
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/projects", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "Get all projects", description = "Retrieve a list of all projects", responses = {
            @ApiResponse(responseCode = "200", description = "List of projects", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Project.class))))
    })
    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    public Flux<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    @Operation(summary = "Get project by ID", description = "Retrieve a specific project by its ID", parameters = {
            @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "Project ID", schema = @Schema(type = "integer", format = "int64", example = "1"))
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Project found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Project.class))),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    public Mono<ResponseEntity<Project>> getProjectById(@PathVariable Long id) {
        return projectService
                .getProjectById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Create a new project", description = "Create a new project with specified details", responses = {
            @ApiResponse(responseCode = "200", description = "Project created", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Project.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_EDIT')")
    public Mono<ResponseEntity<Project>> createProject(@RequestBody Project project) {
        return projectService
                .createProject(project)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @Operation(summary = "Update an existing project", description = "Update project information by ID", parameters = {
            @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "Project ID", schema = @Schema(type = "integer", format = "int64", example = "1"))
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Project updated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Project.class))),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_EDIT')")
    public Mono<ResponseEntity<Project>> updateProject(@PathVariable Long id, @RequestBody Project project) {
        return projectService
                .updateProject(id, project)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    if (error.getMessage().contains("not found")) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @Operation(summary = "Delete a project", description = "Delete a project by ID from database", parameters = {
            @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "Project ID", schema = @Schema(type = "integer", format = "int64", example = "1"))
    }, responses = {
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_EDIT')")
    public Mono<ResponseEntity<Void>> deleteProject(@PathVariable Long id) {
        return projectService
                .deleteProject(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }
}
