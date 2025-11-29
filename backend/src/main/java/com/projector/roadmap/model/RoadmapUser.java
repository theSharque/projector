package com.projector.roadmap.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table("roadmap_users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapUser {

    @Id
    @Column("roadmap_id")
    private Long roadmapId;

    @Column("user_id")
    private Long userId;
}
