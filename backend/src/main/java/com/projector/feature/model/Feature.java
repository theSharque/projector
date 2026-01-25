package com.projector.feature.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table("features")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feature {

    @Schema(description = "Unique ID of feature", example = "1")
    @Id
    private Long id;

    @Schema(description = "Year (2000-2500)", example = "2024")
    @NotNull
    @Min(2000)
    @Max(2500)
    @Column("year")
    private Long year;

    @Schema(description = "Quarter (Q1, Q2, Q3, Q4)", example = "Q1")
    @NotNull
    @Column("quarter")
    private Quarter quarter;

    @Schema(description = "Feature creation date", example = "2024-01-01T00:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    @Column("create_date")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createDate;

    @Schema(description = "Feature last update date", example = "2024-01-02T00:00:00")
    @Column("update_date")
    private LocalDateTime updateDate;

    @Schema(description = "Author user ID", example = "1")
    @NotNull
    @Column("author_id")
    private Long authorId;

    @Schema(description = "Sprint number", example = "1")
    @Column("sprint")
    private Long sprint;

    @Schema(description = "Release name", example = "v1.0.0")
    @Column("release")
    private String release;

    @Schema(description = "Feature summary", example = "User authentication feature")
    @Column("summary")
    private String summary;

    @Schema(description = "Feature description", example = "Detailed description of the feature")
    @Column("description")
    private String description;

    @Schema(description = "Functional area IDs", example = "[1, 2, 3]")
    @NotNull
    @Column("functional_area_ids")
    private List<Long> functionalAreaIds;
}
