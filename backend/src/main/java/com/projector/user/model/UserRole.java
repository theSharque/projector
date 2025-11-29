package com.projector.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table("user_roles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    @Id
    @Column("user_id")
    private Long userId;

    @Column("role_id")
    private Long roleId;
}
