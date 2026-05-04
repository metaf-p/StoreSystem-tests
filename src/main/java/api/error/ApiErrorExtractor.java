package api.error;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import model.error.ApiError;
import model.error.ApiErrorResponse;
import model.error.ValidationError;

import java.util.List;
import java.util.Map;

public class ApiErrorExtractor {
    private ApiErrorExtractor() {
    }

    public static ApiErrorResponse extract(Response response) {
        JsonPath jsonPath = response.then().extract().jsonPath();
        Object detail = jsonPath.get("detail");

        if (detail instanceof String) {
            ApiError apiError = response.as(ApiError.class);
            return ApiErrorResponse.simple(apiError);
        }

        if (detail instanceof List<?> details) {
            List<ValidationError> validationErrors = details.stream()
                    .map(ApiErrorExtractor::toValidationError)
                    .toList();

            return ApiErrorResponse.validation(validationErrors);
        }

        throw new IllegalStateException("Unsupported API error response format: " + detail);
    }

    @SuppressWarnings("unchecked")
    private static ValidationError toValidationError(Object value) {
        if (!(value instanceof Map<?, ?> rawError)) {
            throw new IllegalStateException("Validation error item has unsupported format: " + value);
        }

        List<String> loc = (List<String>) rawError.get("loc");
        String msg = (String) rawError.get("msg");
        String type = (String) rawError.get("type");

        return new ValidationError(loc, msg, type);
    }
}
