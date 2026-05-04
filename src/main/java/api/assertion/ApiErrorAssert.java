package api.assertion;

import api.error.ApiErrorExtractor;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import model.error.ApiErrorResponse;
import org.assertj.core.api.Assertions;

public class ApiErrorAssert {
    private final ApiErrorResponse actual;

    private ApiErrorAssert(ApiErrorResponse actual) {
        this.actual = actual;
    }

    public static ApiErrorAssert assertThat(
            Response response,
            ResponseSpecification responseSpecification
    ) {
        response.then().spec(responseSpecification);
        ApiErrorResponse errorResponse = ApiErrorExtractor.extract(response);

        return new ApiErrorAssert(errorResponse);
    }

    public ApiErrorAssert hasDetail(String expectedDetail) {
        Assertions.assertThat(actual.detail()).isEqualTo(expectedDetail);
        return this;
    }

    public ApiErrorAssert hasFirstValidationMessage(String expectedMessage) {
        Assertions.assertThat(actual.firstValidationMessage()).isEqualTo(expectedMessage);
        return this;
    }

    public ApiErrorAssert hasFirstValidationType(String expectedType) {
        Assertions.assertThat(actual.firstValidationType()).isEqualTo(expectedType);
        return this;
    }

    public ApiErrorAssert hasFirstValidationField(String expectedField) {
        Assertions.assertThat(actual.firstValidationField()).isEqualTo(expectedField);
        return this;
    }
}
