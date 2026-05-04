package model.error;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ValidationError(
        @JsonProperty("loc")
        List<String> loc,
        @JsonProperty("msg")
        String msg,
        @JsonProperty("type")
        String type
) {
}
