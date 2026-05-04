package api.client;

import api.transport.ApiRequest;
import api.transport.ApiRequester;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public abstract class BaseApiClient {
    private final ApiRequester apiRequester;

    public BaseApiClient(ApiRequester apiRequester) {
        this.apiRequester = apiRequester;
    }

    protected <RES> RES execute(
            ApiRequest<RES> apiRequest,
            RequestSpecification requestSpecification,
            ResponseSpecification responseSpecification
    ) {
        return apiRequester.execute(
                apiRequest,
                requestSpecification,
                responseSpecification
        );
    }

    protected <RES> Response executeRaw(
            ApiRequest<RES> apiRequest,
            RequestSpecification requestSpecification
    ) {
        return apiRequester.executeRaw(
                apiRequest,
                requestSpecification
        );
    }
}
