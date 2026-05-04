package model.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import model.auth.common.UserRole;

public record PromoteResponse(
        @JsonProperty("detail")
        String detail,
        @JsonProperty("role")
        UserRole role
) {
}
