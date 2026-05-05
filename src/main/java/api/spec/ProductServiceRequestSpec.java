package api.spec;

import api.logging.ApiLoggingFilter;
import config.Config;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import model.auth.common.AuthContext;

public final class ProductServiceRequestSpec {
    private ProductServiceRequestSpec() {}

    private static RequestSpecBuilder defaultRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(Config.getInstance().productServiceUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(ApiLoggingFilter.filters());
    }

    public static RequestSpecification unauthenticatedRequest() {
        return defaultRequestSpec()
                .build();
    }

    public static RequestSpecification authenticatedRequest(AuthContext context) {
        return defaultRequestSpec()
                .addHeader("Authorization", context.tokenType() + " " + context.token())
                .build();
    }
}
