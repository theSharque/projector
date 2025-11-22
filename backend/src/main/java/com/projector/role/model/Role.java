package com.projector.role.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("roles")
@NoArgsConstructor
public class Role {

    @Schema(description = "Unique ID of role", example = "1")
    @Id
    private Long id;

    @Schema(description = "Name of the role", example = "Admin")
    @NotBlank
    @NotNull
    private String name;

    @Schema(description = "Authorities stored as comma-separated string in database", example = "USER_VIEW,USER_EDIT,ROLE_VIEW")
    @Column("authorities")
    @JsonIgnore
    private String authoritiesString;

    @Schema(description = "Set of authorities", example = "[\"USER_VIEW\", \"USER_EDIT\", \"ROLE_VIEW\"]", accessMode = Schema.AccessMode.READ_ONLY)
    @Transient
    @JsonProperty("authorities")
    private Set<String> authorities;

    @Builder
    public Role(Long id, String name, String authoritiesString) {
        this.id = id;
        this.name = name;
        this.authoritiesString = authoritiesString;
    }

    public Set<String> getAuthorities() {
        if (authorities != null) {
            return authorities;
        }
        if (authoritiesString == null || authoritiesString.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(authoritiesString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities != null ? new HashSet<>(authorities) : Collections.emptySet();
        this.authoritiesString = this.authorities.isEmpty()
                ? null
                : String.join(",", this.authorities);
    }

    public static Role fromAuthoritiesString(Long id, String name, String authoritiesString) {
        Role role = Role.builder()
                .id(id)
                .name(name)
                .authoritiesString(authoritiesString)
                .build();
        role.getAuthorities();
        return role;
    }
}

