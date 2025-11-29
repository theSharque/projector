package com.projector.feature.controller;

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

import com.projector.feature.model.Feature;
import com.projector.feature.service.FeatureService;

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
@RequestMapping(value = "/api/features", produces = MediaType.APPLICATION_JSON_VALUE)
public class FeatureController {

    private final FeatureService featureService;

    @Operation(summary = "Get all features", description = "Retrieve a list of all features", responses = {
            @ApiResponse(responseCode = "200", description = "List of features", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Feature.class))))
    })
    @GetMapping
    @PreAuthorize("hasAuthority('FEATURE_VIEW')")
    public Flux<Feature> getAllFeatures() {
        return featureService.getAllFeatures();
    }

    @Operation(summary = "Get feature by ID", description = "Retrieve a specific feature by its ID", parameters = {
            @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "Feature ID", schema = @Schema(type = "integer", format = "int64", example = "1"))
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Feature found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Feature.class))),
            @ApiResponse(responseCode = "404", description = "Feature not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FEATURE_VIEW')")
    public Mono<ResponseEntity<Feature>> getFeatureById(@PathVariable Long id) {
        return featureService
                .getFeatureById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Create a new feature", description = "Create a new feature with specified details", responses = {
            @ApiResponse(responseCode = "200", description = "Feature created", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Feature.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('FEATURE_EDIT')")
    public Mono<ResponseEntity<Feature>> createFeature(@RequestBody Feature feature) {
        return featureService
                .createFeature(feature)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @Operation(summary = "Update an existing feature", description = "Update feature information by ID", parameters = {
            @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "Feature ID", schema = @Schema(type = "integer", format = "int64", example = "1"))
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Feature updated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Feature.class))),
            @ApiResponse(responseCode = "404", description = "Feature not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FEATURE_EDIT')")
    public Mono<ResponseEntity<Feature>> updateFeature(@PathVariable Long id, @RequestBody Feature feature) {
        return featureService
                .updateFeature(id, feature)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    if (error.getMessage().contains("not found")) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @Operation(summary = "Delete a feature", description = "Delete a feature by ID from database", parameters = {
            @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "Feature ID", schema = @Schema(type = "integer", format = "int64", example = "1"))
    }, responses = {
            @ApiResponse(responseCode = "204", description = "Feature deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Feature not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FEATURE_EDIT')")
    public Mono<ResponseEntity<Void>> deleteFeature(@PathVariable Long id) {
        return featureService
                .deleteFeature(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }
}

