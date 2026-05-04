package model.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import model.auth.common.UserRole;

public record PromoteRequest(
        @JsonProperty("role")
        UserRole role
) {
}
