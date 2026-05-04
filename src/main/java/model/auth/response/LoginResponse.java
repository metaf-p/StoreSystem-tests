package model.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record LoginResponse(
        @JsonProperty("user_id")
        UUID userId,
        @JsonProperty("message")
        String message,
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("token_type")
        String tokenType
) {
}
