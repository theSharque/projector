package com.projector.project.model;

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
@Table("projects")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Schema(description = "Unique ID of project", example = "1")
    @Id
    private Long id;

    @Schema(description = "Project name", example = "My Project")
    @NotBlank
    @NotNull
    @Column("name")
    private String name;

    @Schema(description = "Project creation date", example = "2024-01-01T00:00:00")
    @NotNull
    @Column("create_date")
    private LocalDateTime createDate;

    @Schema(description = "Author user ID", example = "1")
    @NotNull
    @Column("author_id")
    private Long authorId;

    @Schema(description = "Project mission", example = "To build amazing software")
    @Column("mission")
    private String mission;

    @Schema(description = "Project description", example = "Detailed project description")
    @Column("description")
    private String description;

    @Schema(description = "List of participant user IDs", example = "[1, 2, 3]")
    @Transient
    @JsonProperty("participantIds")
    private List<Long> participantIds;
}
