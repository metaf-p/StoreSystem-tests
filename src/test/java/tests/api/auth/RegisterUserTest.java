package tests.api.auth;

import api.assertion.ApiErrorAssert;
import api.client.ApiClients;
import api.spec.ResponseSpec;
import data.auth.AuthTestData;
import data.auth.AuthUserFixture;
import io.restassured.response.Response;
import jupiter.annotation.ApiTest;
import model.auth.common.UserRole;
import model.auth.request.RegisterUserRequest;
import model.auth.response.RegisterUserResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ApiTest
public class RegisterUserTest {
    private final ApiClients api = ApiClients.create();

    private static final String SUCCESSFUL_REGISTRATION_MESSAGE = "User successfully created";
    private static final String EMAIL_ALREADY_REGISTERED_MESSAGE = "Email already registered";

    @Test
    void shouldRegisterUserWithValidInput(
            AuthUserFixture authUserFixture
    ) {
        RegisterUserRequest request = AuthTestData.uniqueUser();
        RegisterUserResponse response = authUserFixture.registerUser(request);

        assertThat(response.message()).isEqualTo(SUCCESSFUL_REGISTRATION_MESSAGE);
        assertThat(response.user().email()).isEqualTo(request.email());
        assertThat(response.user().role()).isEqualTo(UserRole.CUSTOMER.value());
    }

    @Test
    void shouldNotRegisterUserWithExistingEmail() {
        RegisterUserRequest request = AuthTestData.uniqueUser();
        api.auth().register(request);

        RegisterUserRequest requestWithDuplicateEmail = AuthTestData.newUserWithEmail(request.email());

        Response response = api.auth().registerRaw(requestWithDuplicateEmail);

        ApiErrorAssert.assertThat(response, ResponseSpec.unprocessableEntity422())
                .hasDetail(EMAIL_ALREADY_REGISTERED_MESSAGE);
    }
}
