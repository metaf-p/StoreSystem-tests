package model.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import model.auth.common.User;

public record RegisterUserResponse(
        @JsonProperty("message")
        String message,
        @JsonProperty("user")
        User user
) {
}
