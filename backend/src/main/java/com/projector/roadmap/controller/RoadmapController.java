package com.projector.roadmap.controller;

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
import org.springframework.web.server.ServerWebInputException;

import com.projector.roadmap.model.Roadmap;
import com.projector.roadmap.service.RoadmapService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
})
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/roadmaps", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoadmapController {

    private final RoadmapService roadmapService;

    @Operation(summary = "Get all roadmaps", description = "Retrieve a list of all roadmaps")
    @ApiResponse(responseCode = "200", description = "List of roadmaps", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Roadmap.class))))
    @GetMapping
    @PreAuthorize("hasAuthority('ROADMAP_VIEW')")
    public Flux<Roadmap> getAllRoadmaps() {
        return roadmapService.getAllRoadmaps();
    }

    @Operation(summary = "Get roadmap by ID", description = "Retrieve a specific roadmap by its ID")
    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "Roadmap ID", schema = @Schema(type = "integer", format = "int64", example = "1"))
    @ApiResponse(responseCode = "200", description = "Roadmap found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Roadmap.class)))
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROADMAP_VIEW')")
    public Mono<ResponseEntity<Roadmap>> getRoadmapById(@PathVariable Long id) {
        return roadmapService
                .getRoadmapById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Create a new roadmap", description = "Create a new roadmap with specified details")
    @ApiResponse(responseCode = "200", description = "Roadmap created", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Roadmap.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping
    @PreAuthorize("hasAuthority('ROADMAP_EDIT')")
    public Mono<ResponseEntity<Roadmap>> createRoadmap(@RequestBody Roadmap roadmap) {
        return roadmapService
                .createRoadmap(roadmap)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("Error creating roadmap: {}", error.getMessage(), error);
                    if (error instanceof ServerWebInputException) {
                        return Mono.just(ResponseEntity.badRequest().build());
                    }
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @Operation(summary = "Update an existing roadmap", description = "Update roadmap information by ID")
    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "Roadmap ID", schema = @Schema(type = "integer", format = "int64", example = "1"))
    @ApiResponse(responseCode = "200", description = "Roadmap updated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Roadmap.class)))
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROADMAP_EDIT')")
    public Mono<ResponseEntity<Roadmap>> updateRoadmap(@PathVariable Long id, @RequestBody Roadmap roadmap) {
        return roadmapService
                .updateRoadmap(id, roadmap)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    if (error.getMessage().contains("not found")) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @Operation(summary = "Delete a roadmap", description = "Delete a roadmap by ID from database")
    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "Roadmap ID", schema = @Schema(type = "integer", format = "int64", example = "1"))
    @ApiResponse(responseCode = "204", description = "Roadmap deleted successfully")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROADMAP_EDIT')")
    public Mono<ResponseEntity<Void>> deleteRoadmap(@PathVariable Long id) {
        return roadmapService
                .deleteRoadmap(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }
}
