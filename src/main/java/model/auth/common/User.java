package model.auth.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record User(
        @JsonProperty("id")
        UUID userId,
        @JsonProperty("name")
        String name,
        @JsonProperty("email")
        String email,
        @JsonProperty("role")
        String role

) {
}
