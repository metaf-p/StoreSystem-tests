package model.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeleteUserResponse(
        @JsonProperty("detail")
        String detail
) {
}
