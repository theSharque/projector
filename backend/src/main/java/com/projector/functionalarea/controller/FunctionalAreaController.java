package com.projector.functionalarea.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projector.functionalarea.model.FunctionalArea;
import com.projector.functionalarea.service.FunctionalAreaService;

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
@RequestMapping(value = "/api/functional-areas", produces = MediaType.APPLICATION_JSON_VALUE)
public class FunctionalAreaController {

    private final FunctionalAreaService functionalAreaService;

    @Operation(summary = "Get all functional areas", description = "Retrieve a list of all functional areas")
    @ApiResponse(responseCode = "200", description = "List of functional areas", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = FunctionalArea.class))))
    @GetMapping
    @PreAuthorize("hasAuthority('FA_VIEW')")
    public Flux<FunctionalArea> getAllFunctionalAreas() {
        return functionalAreaService.getAllFunctionalAreas();
    }

    @Operation(summary = "Get functional area by ID", description = "Retrieve a specific functional area by its ID")
    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "Functional area ID", schema = @Schema(type = "integer", format = "int64", example = "1"))
    @ApiResponse(responseCode = "200", description = "Functional area found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FunctionalArea.class)))
    @ApiResponse(responseCode = "404", description = "Functional area not found")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FA_VIEW')")
    public Mono<ResponseEntity<FunctionalArea>> getFunctionalAreaById(@PathVariable Long id) {
        return functionalAreaService
                .getFunctionalAreaById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Get functional area usage", description = "Get count of features using this functional area")
    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "Functional area ID", schema = @Schema(type = "integer", format = "int64", example = "1"))
    @ApiResponse(responseCode = "200", description = "Usage count", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Long.class)))
    @GetMapping("/{id}/usage")
    @PreAuthorize("hasAuthority('FA_VIEW')")
    public Mono<ResponseEntity<Long>> getFunctionalAreaUsage(@PathVariable Long id) {
        return functionalAreaService
                .getFeaturesUsingFunctionalArea(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.ok(0L));
    }

    @Operation(summary = "Create a new functional area", description = "Create a new functional area with specified details")
    @ApiResponse(responseCode = "200", description = "Functional area created", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FunctionalArea.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping
    @PreAuthorize("hasAuthority('FA_EDIT')")
    public Mono<ResponseEntity<FunctionalArea>> createFunctionalArea(@RequestBody FunctionalArea functionalArea) {
        return functionalAreaService
                .createFunctionalArea(functionalArea)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @Operation(summary = "Update an existing functional area", description = "Update functional area information by ID")
    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "Functional area ID", schema = @Schema(type = "integer", format = "int64", example = "1"))
    @ApiResponse(responseCode = "200", description = "Functional area updated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FunctionalArea.class)))
    @ApiResponse(responseCode = "404", description = "Functional area not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FA_EDIT')")
    public Mono<ResponseEntity<FunctionalArea>> updateFunctionalArea(@PathVariable Long id, @RequestBody FunctionalArea functionalArea) {
        return functionalAreaService
                .updateFunctionalArea(id, functionalArea)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    if (error.getMessage().contains("not found")) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @Operation(summary = "Delete a functional area", description = "Delete a functional area by ID with required replacement functional area")
    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "Functional area ID to delete", schema = @Schema(type = "integer", format = "int64", example = "1"))
    @Parameter(in = ParameterIn.QUERY, name = "replacementFaId", required = true, description = "Replacement functional area ID", schema = @Schema(type = "integer", format = "int64", example = "2"))
    @ApiResponse(responseCode = "204", description = "Functional area deleted successfully")
    @ApiResponse(responseCode = "404", description = "Functional area not found")
    @ApiResponse(responseCode = "400", description = "Invalid input or replacement ID not provided")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FA_EDIT')")
    public Mono<ResponseEntity<Void>> deleteFunctionalArea(@PathVariable Long id, @RequestParam Long replacementFaId) {
        return functionalAreaService
                .deleteFunctionalArea(id, replacementFaId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(error -> {
                    if (error.getMessage().contains("not found")) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }
}
