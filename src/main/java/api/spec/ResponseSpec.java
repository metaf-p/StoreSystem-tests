package api.spec;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

public final class ResponseSpec {
    private ResponseSpec() {}

    private static ResponseSpecBuilder defaultSpecBuilder() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification ok200() {
        return defaultSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification created201() {
        return  defaultSpecBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    public static ResponseSpecification forbidden403() {
        return defaultSpecBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .build();
    }

    public static ResponseSpecification unauthorized401() {
        return defaultSpecBuilder()
                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                .build();
    }

    public static ResponseSpecification unprocessableEntity422() {
        return defaultSpecBuilder()
                .expectStatusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .build();
    }

    public static ResponseSpecification badRequest400() {
        return defaultSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .build();
    }

    public static ResponseSpecification notFound404() {
        return defaultSpecBuilder()
                .expectStatusCode(HttpStatus.SC_NOT_FOUND)
                .build();
    }

    public static ResponseSpecification deleteQuietly() {
        return defaultSpecBuilder()
                .expectStatusCode(Matchers.anyOf(
                        Matchers.is(HttpStatus.SC_OK),
                        Matchers.is(HttpStatus.SC_NO_CONTENT),
                        Matchers.is(HttpStatus.SC_NOT_FOUND)))
                .build();
    }
}
