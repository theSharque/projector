package com.projector.user.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table("users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Schema(description = "Unique ID of user", example = "1")
    @Id
    private Long id;

    @Schema(description = "User email address", example = "user@example.com")
    @Email
    @NotBlank
    @NotNull
    @Column("email")
    private String email;

    @Schema(description = "User password", example = "password123")
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Schema(description = "Password hash", example = "hashed_password")
    @Column("pass_hash")
    @JsonIgnore
    private String passHash;

    @Schema(description = "List of role IDs assigned to the user", example = "[1, 2]")
    @Transient
    @JsonProperty("roleIds")
    private List<Long> roleIds;

    public static User forCookie(User user) {
        return User.builder().id(user.getId()).email(user.getEmail()).build();
    }
}
