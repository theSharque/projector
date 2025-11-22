package com.projector.core.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User credentials for login")
public class UserCredentials {

    @Schema(description = "User email address", example = "admin")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "User password", example = "admin")
    @NotBlank
    private String password;
}

