package com.projector.task.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table("tasks")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Schema(description = "Unique ID of task", example = "1")
    @Id
    private Long id;

    @Schema(description = "Feature ID this task belongs to", example = "1")
    @NotNull
    @Column("feature_id")
    private Long featureId;

    @Schema(description = "Roadmap ID this task belongs to", example = "1")
    @NotNull
    @Column("roadmap_id")
    private Long roadmapId;

    @Schema(description = "Task summary", example = "Implement user authentication")
    @Column("summary")
    private String summary;

    @Schema(description = "Task description", example = "Detailed description of the task")
    @Column("description")
    private String description;

    @Schema(description = "Task creation date", example = "2024-01-01T00:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    @Column("create_date")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createDate;

    @Schema(description = "Task last update date", example = "2024-01-02T00:00:00")
    @Column("update_date")
    private LocalDateTime updateDate;

    @Schema(description = "Author user ID", example = "1")
    @NotNull
    @Column("author_id")
    private Long authorId;
}
