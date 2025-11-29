package com.projector.roadmap.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table("roadmaps")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Roadmap {

    @Schema(description = "Unique ID of roadmap", example = "1")
    @Id
    private Long id;

    @Schema(description = "Roadmap project name", example = "My Roadmap")
    @NotBlank
    @NotNull
    @Column("project_name")
    private String projectName;

    @Schema(description = "Roadmap creation date", example = "2024-01-01T00:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    @NotNull
    @Column("create_date")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createDate;

    @Schema(description = "Roadmap last update date", example = "2024-01-02T00:00:00")
    @Column("update_date")
    private LocalDateTime updateDate;

    @Schema(description = "Author user ID", example = "1")
    @NotNull
    @Column("author_id")
    private Long authorId;

    @Schema(description = "Roadmap mission", example = "To build amazing software")
    @Column("mission")
    private String mission;

    @Schema(description = "Roadmap description", example = "Detailed roadmap description")
    @Column("description")
    private String description;

    @Schema(description = "List of participant user IDs", example = "[1, 2, 3]")
    @Transient
    @JsonProperty("participantIds")
    private List<Long> participantIds;
}

