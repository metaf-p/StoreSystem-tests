package model.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegisterUserRequest(
        @JsonProperty("name")
        String name,
        @JsonProperty("email")
        String email,
        @JsonProperty("password")
        String password
) {

}
