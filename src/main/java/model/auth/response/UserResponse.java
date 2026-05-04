package model.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import model.auth.common.Users;

import java.util.List;

public record UserResponse(
        @JsonProperty("users")
        List<Users> users,
        @JsonProperty("total")
        int total,
        @JsonProperty("page")
        int page,
        @JsonProperty("page_size")
        int pageSize,
        @JsonProperty("total_pages")
        int totalPages
) {
}