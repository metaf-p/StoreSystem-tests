package model.auth.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record Users(
        @JsonProperty("id")
        UUID userId,
        @JsonProperty("name")
        String name,
        @JsonProperty("email")
        String email,
        @JsonProperty("role")
        UserRole role
) {
}
