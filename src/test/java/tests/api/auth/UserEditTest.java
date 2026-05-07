package tests.api.auth;

import api.assertion.ApiErrorAssert;
import api.client.ApiClients;
import api.spec.ResponseSpec;
import data.auth.AuthTestData;
import data.auth.AuthUserFixture;
import io.restassured.response.Response;
import jupiter.annotation.Admin;
import jupiter.annotation.CurrentUser;
import jupiter.annotation.TestUser;
import jupiter.annotation.meta.ApiTest;
import model.auth.common.AuthContext;
import model.auth.common.UserRole;
import model.auth.request.EditUserRequest;
import model.auth.response.CurrentUserResponse;
import model.auth.response.EditUserResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
public class UserEditTest {
    private final ApiClients api = ApiClients.create();

    private static final String USER_SUCCESSFULLY_UPDATED_MESSAGE = "User successfully updated";
    private static final String EMAIL_ALREADY_REGISTERED_MESSAGE = "Email already registered";
    private static final String INSUFFICIENT_RIGHTS_MESSAGE = "Insufficient rights";

    @TestUser
    @Test
    void shouldEditUserAsAdminWithValidData(
            @Admin AuthContext admin,
            @CurrentUser AuthContext user
    ) {
        EditUserRequest editUserRequest = AuthTestData.editUserRequest();
        EditUserResponse editUserResponse = api.users().edit(admin, editUserRequest, user.userId());

        assertThat(editUserResponse.detail())
                .isEqualTo(USER_SUCCESSFULLY_UPDATED_MESSAGE);
        assertThat(editUserResponse.user().email())
                .isEqualTo(editUserRequest.email());
        assertThat(editUserResponse.user().name())
                .isEqualTo(editUserRequest.name());

        CurrentUserResponse profile = api.users().profile(user);
        assertThat(profile.email()).isEqualTo(editUserRequest.email());
        assertThat(profile.name()).isEqualTo(editUserRequest.name());
    }

    @TestUser
    @Test
    void shouldEditUserEmailAsAdmin(
            @Admin AuthContext admin,
            @CurrentUser AuthContext user
    ) {
        String nameBeforeEdit = api.users().profile(user).name();
        EditUserRequest editUserRequest = AuthTestData.editUserRequestWithEmailOnly();

        EditUserResponse editUserResponse = api.users().edit(admin, editUserRequest, user.userId());

        assertThat(editUserResponse.detail())
                .isEqualTo(USER_SUCCESSFULLY_UPDATED_MESSAGE);
        assertThat(editUserResponse.user().email())
                .isEqualTo(editUserRequest.email());

        CurrentUserResponse profileAfterEdit = api.users().profile(user);
        assertThat(profileAfterEdit.email()).isEqualTo(editUserRequest.email());
        assertThat(profileAfterEdit.name()).isEqualTo(nameBeforeEdit);
    }

    @TestUser
    @Test
    void shouldEditUserNameAsAdmin(
            @Admin AuthContext admin,
            @CurrentUser AuthContext user
    ) {
        String emailBeforeEdit = api.users().profile(user).email();
        EditUserRequest editUserRequest = AuthTestData.editUserRequestWithNameOnly();

        EditUserResponse editUserResponse = api.users().edit(admin, editUserRequest, user.userId());

        assertThat(editUserResponse.detail())
                .isEqualTo(USER_SUCCESSFULLY_UPDATED_MESSAGE);
        assertThat(editUserResponse.user().name())
                .isEqualTo(editUserRequest.name());

        CurrentUserResponse profileAfterEdit = api.users().profile(user);
        assertThat(profileAfterEdit.name()).isEqualTo(editUserRequest.name());
        assertThat(profileAfterEdit.email()).isEqualTo(emailBeforeEdit);
    }

    @TestUser(role = UserRole.OPERATOR)
    @Test
    void forbidEditProfileForOperator(
            @CurrentUser AuthContext user,
            AuthUserFixture authUserFixture
    ) {
        AuthContext editor = authUserFixture.createUser();

        EditUserRequest editUserRequest = AuthTestData.editUserRequest();
        Response response = api.users().editRaw(editor, editUserRequest, user.userId());

        ApiErrorAssert.assertThat(response, ResponseSpec.forbidden403())
                .hasDetail(INSUFFICIENT_RIGHTS_MESSAGE);
    }

    @TestUser
    @Test
    void rejectEditProfileWithInvalidEmail(
            @Admin AuthContext admin,
            @CurrentUser AuthContext user
    ) {
        EditUserRequest editUserRequest = AuthTestData.editUserRequestWithEmail("invalid-email");
        Response response = api.users().editRaw(admin, editUserRequest, user.userId());

        ApiErrorAssert.assertThat(response, ResponseSpec.unprocessableEntity422())
                .hasFirstValidationType("value_error.email")
                .hasFirstValidationMessage("value is not a valid email address")
                .hasFirstValidationField("email");
    }

    @TestUser
    @Test
    void shouldRejectEditProfileWhenEmailAlreadyExists(
            @Admin AuthContext admin,
            @CurrentUser AuthContext user,
            AuthUserFixture authUserFixture
    ) {
        AuthContext emailProvider = authUserFixture.createUser();
        String email = api.users().profile(emailProvider).email();
        EditUserRequest editUserRequest = AuthTestData.editUserRequestWithEmail(email);

        Response response = api.users().editRaw(admin, editUserRequest, user.userId());

        ApiErrorAssert.assertThat(response, ResponseSpec.unprocessableEntity422())
                .hasDetail(EMAIL_ALREADY_REGISTERED_MESSAGE);
    }
}
