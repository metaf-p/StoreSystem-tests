package model.auth.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EditUserRequest(
        @JsonProperty("email")
        String email,
        @JsonProperty("name")
        String name
) {
}
