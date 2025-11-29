package com.projector.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.projector.user.model.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserClaims {

    @JsonProperty("user")
    private User user;

    @JsonProperty("authorities")
    private List<String> authorities;
}
