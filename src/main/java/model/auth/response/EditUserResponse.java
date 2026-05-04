package model.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import model.auth.common.User;

public record EditUserResponse(
        @JsonProperty("detail")
        String detail,
        @JsonProperty("user")
        User user
) {
}
