package tests.api.auth;

import api.assertion.ApiErrorAssert;
import api.client.ApiClients;
import api.spec.ResponseSpec;
import io.restassured.response.Response;
import jupiter.annotation.Admin;
import jupiter.annotation.ApiTest;
import model.auth.common.AuthContext;
import model.auth.common.UserRole;
import model.auth.request.PromoteRequest;
import model.auth.response.DeleteUserResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
public class UserDeleteTest {
    private final ApiClients api = ApiClients.create();

    private static final String USER_DELETED_SUCCESSFULLY_MESSAGE = "User successfully deleted";
    private static final String SUPERADMIN_CANNOT_DELETE_OWN_ACCOUNT_MESSAGE = "Super admin cannot delete own account";
    private static final String INVALID_UUID_FORMAT_MESSAGE = "Invalid UUID format";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String NON_EXISTENT_USER_ID = "00000000-0000-0000-0000-000000000000";
    private static final String INVALID_USER_ID = "invalid-id";

    @Test
    void shouldDeleteExistingUser(
            @Admin AuthContext admin,
            AuthContext user
    ) {
        DeleteUserResponse deleteUserResponse = api.users().delete(admin, user.userId());

        assertThat(deleteUserResponse.detail()).isEqualTo(USER_DELETED_SUCCESSFULLY_MESSAGE);

        Response response = api.users().profileRaw(user);
        ApiErrorAssert.assertThat(response, ResponseSpec.notFound404())
                .hasDetail(USER_NOT_FOUND_MESSAGE);
    }

    @Test
    void shouldNotAllowDeleteThemselvesForAdmin(
            @Admin AuthContext admin,
            AuthContext user
    ) {
        PromoteRequest request = new PromoteRequest(UserRole.ADMIN);

        api.users().promote(admin, user.userId(), request);

        Response response = api.users().deleteRaw(user, user.userId());

        ApiErrorAssert.assertThat(response, ResponseSpec.forbidden403())
                .hasDetail(SUPERADMIN_CANNOT_DELETE_OWN_ACCOUNT_MESSAGE);
    }

    @Test
    void shouldRejectDeleteWithInvalidUUIDFormat(
            @Admin AuthContext admin
    ) {
        Response response = api.users().deleteRaw(admin, INVALID_USER_ID);

        ApiErrorAssert.assertThat(response, ResponseSpec.badRequest400())
                .hasDetail(INVALID_UUID_FORMAT_MESSAGE);
    }

    @Test
    void shouldReturnNotFoundDeleteWhenUserNotExist(
            @Admin AuthContext admin
    ) {
        Response response = api.users().deleteRaw(admin, NON_EXISTENT_USER_ID);

        ApiErrorAssert.assertThat(response, ResponseSpec.notFound404())
                .hasDetail(USER_NOT_FOUND_MESSAGE);
    }
}
