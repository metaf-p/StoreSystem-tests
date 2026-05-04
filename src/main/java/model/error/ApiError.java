package model.error;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApiError(
        @JsonProperty("detail")
        String detail
) {
}
