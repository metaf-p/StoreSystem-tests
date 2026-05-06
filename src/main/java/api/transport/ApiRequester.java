package api.transport;

import api.endpoint.Endpoint;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class ApiRequester {

    public <RES> RES execute(
            ApiRequest<RES> apiRequest,
            RequestSpecification requestSpecification,
            ResponseSpecification responseSpecification
    ) {
        return executeRaw(apiRequest, requestSpecification)
                .then()
                .spec(responseSpecification)
                .extract()
                .as(apiRequest.endpoint().responseTypeRef());
    }

    public Response executeRaw(
            ApiRequest<?> apiRequest,
            RequestSpecification requestSpecification
    ) {
        RequestSpecification request = given()
                .spec(requestSpecification);

        if (!apiRequest.pathParams().isEmpty()) {
            request.pathParams(apiRequest.pathParams());
        }

        if (!apiRequest.queryParams().isEmpty()) {
            request.queryParams(apiRequest.queryParams());
        }

        for(MultipartPart part : apiRequest.multipartParts()) {
            if(part.isFile()) {
                request.multiPart(part.controlName(), part.file(), part.mimeType());
            } else {
                request.multiPart(part.controlName(), part.value());
            }
        }

        if (apiRequest.body() != null) {
            request.body(apiRequest.body());
        }

        return send(request, apiRequest);
    }

    private Response send(
            RequestSpecification requestSpecification,
            ApiRequest<?> apiRequest
    ) {
        Endpoint<?> endpoint = apiRequest.endpoint();

        return switch (endpoint.httpMethod()) {
            case GET -> requestSpecification.get(endpoint.pathTemplate());
            case POST -> requestSpecification.post(endpoint.pathTemplate());
            case PUT -> requestSpecification.put(endpoint.pathTemplate());
            case DELETE -> requestSpecification.delete(endpoint.pathTemplate());
        };
    }
}
