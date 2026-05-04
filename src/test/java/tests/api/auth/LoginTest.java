package tests.api.auth;

import api.assertion.ApiErrorAssert;
import api.client.ApiClients;
import api.spec.ResponseSpec;
import config.Config;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import jupiter.annotation.ApiTest;
import model.auth.request.LoginRequest;
import model.auth.response.LoginResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ApiTest
public class LoginTest {
    private final ApiClients api = ApiClients.create();

    private static final String SUCCESSFUL_LOGIN_MESSAGE = "User successfully logged in";
    private static final String INVALID_LOGIN_OR_PASSWORD_MESSAGE = "Invalid email or password";

    @Test
    void shouldLoginUserWithValidCredentials() {

        LoginRequest request = new LoginRequest(
                Config.getInstance().adminLogin(),
                Config.getInstance().adminPassword()
        );

        ExtractableResponse<Response> extract = api.auth().loginRaw(request).then()
                .spec(ResponseSpec.ok200())
                .extract();

        String refreshToken = extract.cookie("refresh_token");
        LoginResponse loginResponse = extract.as(LoginResponse.class);
        assertThat(loginResponse.message()).isEqualTo(SUCCESSFUL_LOGIN_MESSAGE);
        assertThat(loginResponse.tokenType()).isEqualTo("bearer");
        assertThat(loginResponse.accessToken()).isNotBlank();
        assertThat(refreshToken).isNotNull().isNotBlank();
    }

    @Test
    void shouldNotLoginWithInvalidPassword() {
        LoginRequest request = new LoginRequest(
                Config.getInstance().adminLogin(),
                "wrongpass"
        );

        Response response = api.auth().loginRaw(request);
        ApiErrorAssert.assertThat(response, ResponseSpec.badRequest400())
                .hasDetail(INVALID_LOGIN_OR_PASSWORD_MESSAGE);
    }

}
