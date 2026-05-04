package api.endpoint;

import io.restassured.common.mapper.TypeRef;

public record Endpoint<RES>(
        String pathTemplate,
        HttpMethods httpMethod,
        TypeRef<RES> responseTypeRef
) {
}
