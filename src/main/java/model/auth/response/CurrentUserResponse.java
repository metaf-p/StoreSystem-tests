package model.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import model.auth.common.UserRole;

import java.util.UUID;

public record CurrentUserResponse(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("name")
        String name,
        @JsonProperty("email")
        String email,
        @JsonProperty("role")
        UserRole role
) {
}
