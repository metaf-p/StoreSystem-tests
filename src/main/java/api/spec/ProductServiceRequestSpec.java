package api.spec;

import api.logging.ApiLoggingFilter;
import config.Config;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import model.auth.common.AuthContext;

public final class ProductServiceRequestSpec {
    private static final String productServiceBaseUrl = Config.getInstance().productServiceUrl();

    private ProductServiceRequestSpec() {
    }

    private static RequestSpecBuilder defaultRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(productServiceBaseUrl)
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

    public static RequestSpecification authenticatedMultipartRequest(AuthContext context) {
        return new RequestSpecBuilder()
                .setBaseUri(productServiceBaseUrl)
                .setContentType(ContentType.MULTIPART)
                .setAccept(ContentType.JSON)
                .addFilters(ApiLoggingFilter.filters())
                .addHeader("Authorization", context.tokenType() + " " + context.token())
                .build();
    }

    public static RequestSpecification unauthenticatedMultipartRequest() {
        return new RequestSpecBuilder()
                .setBaseUri(productServiceBaseUrl)
                .setContentType(ContentType.MULTIPART)
                .setAccept(ContentType.JSON)
                .addFilters(ApiLoggingFilter.filters())
                .build();
    }
}
