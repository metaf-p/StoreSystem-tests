package api.client;

import api.endpoint.AuthEndpoints;
import api.spec.AuthServiceRequestSpecs;
import api.spec.ResponseSpec;
import api.transport.ApiRequest;
import api.transport.ApiRequester;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import model.auth.common.AuthContext;
import model.auth.request.LoginRequest;
import model.auth.request.RegisterUserRequest;
import model.auth.response.LoginResponse;
import model.auth.response.RegisterUserResponse;

public class AuthClient extends BaseApiClient {

    public AuthClient(ApiRequester apiRequester) {
        super(apiRequester);
    }

    public LoginResponse login(LoginRequest request) {
        return execute(
                ApiRequest.withBody(AuthEndpoints.LOGIN, request),
                AuthServiceRequestSpecs.baseRequest(),
                ResponseSpec.ok200()
        );
    }

    public AuthContext authenticate(LoginRequest request) {
        ExtractableResponse<Response> extract = loginRaw(request).then()
                .spec(ResponseSpec.ok200())
                .extract();

        LoginResponse loginResponse = extract.as(LoginResponse.class);
        String refreshToken = extract.cookie("refresh_token");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalStateException("Login response does not contain refresh_token cookie");
        }

        return new AuthContext(
                loginResponse.userId(),
                loginResponse.accessToken(),
                refreshToken,
                loginResponse.tokenType()
        );
    }

    public Response loginRaw(LoginRequest request) {
        return executeRaw(
                ApiRequest.withBody(AuthEndpoints.LOGIN, request),
                AuthServiceRequestSpecs.baseRequest()
        );
    }

    public RegisterUserResponse register(RegisterUserRequest request) {
        return execute(
                ApiRequest.withBody(AuthEndpoints.REGISTER, request),
                AuthServiceRequestSpecs.baseRequest(),
                ResponseSpec.created201()
        );
    }

    public Response registerRaw(RegisterUserRequest request) {
        return executeRaw(
                ApiRequest.withBody(AuthEndpoints.REGISTER, request),
                AuthServiceRequestSpecs.baseRequest()
        );
    }
}
