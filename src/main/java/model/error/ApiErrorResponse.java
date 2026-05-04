package model.error;

import java.util.List;

public record ApiErrorResponse(String detail, List<ValidationError> validationErrors) {
    public ApiErrorResponse(String detail, List<ValidationError> validationErrors) {
        this.detail = detail;
        this.validationErrors = validationErrors == null
                ? List.of()
                : List.copyOf(validationErrors);
    }

    public static ApiErrorResponse simple(ApiError apiError) {
        return new ApiErrorResponse(apiError.detail(), List.of());
    }

    public static ApiErrorResponse validation(List<ValidationError> validationErrors) {
        return new ApiErrorResponse(null, validationErrors);
    }

    public boolean isSimpleError() {
        return detail != null;
    }

    public boolean isValidationError() {
        return !validationErrors.isEmpty();
    }

    @Override
    public String detail() {
        if (!isSimpleError()) {
            throw new IllegalStateException("Error response does not contain string detail");
        }

        return detail;
    }

    public ValidationError firstValidationError() {
        if (validationErrors.isEmpty()) {
            throw new IllegalStateException("Error response does not contain validation errors");
        }

        return validationErrors.getFirst();
    }

    public String firstValidationMessage() {
        return firstValidationError().msg();
    }

    public String firstValidationType() {
        return firstValidationError().type();
    }

    public String firstValidationField() {
        List<String> loc = firstValidationError().loc();

        if (loc == null || loc.isEmpty()) {
            throw new IllegalStateException("Validation error does not contain loc");
        }

        return loc.getLast();
    }
}
