package tests.api.auth;

import api.assertion.ApiErrorAssert;
import api.client.ApiClients;
import api.spec.ResponseSpec;
import io.restassured.response.Response;
import jupiter.annotation.Admin;
import jupiter.annotation.CurrentUser;
import jupiter.annotation.TestUser;
import jupiter.annotation.meta.ApiTest;
import model.auth.common.AuthContext;
import model.auth.common.UserRole;
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

    @TestUser
    @Test
    void shouldDeleteExistingUser(
            @Admin AuthContext admin,
            @CurrentUser AuthContext user
    ) {
        DeleteUserResponse deleteUserResponse = api.users().delete(admin, user.userId());

        assertThat(deleteUserResponse.detail()).isEqualTo(USER_DELETED_SUCCESSFULLY_MESSAGE);

        Response response = api.users().profileRaw(user);
        ApiErrorAssert.assertThat(response, ResponseSpec.notFound404())
                .hasDetail(USER_NOT_FOUND_MESSAGE);
    }

    @TestUser(role = UserRole.ADMIN)
    @Test
    void rejectSelfDeleteForAdmin(
            @CurrentUser AuthContext user
    ) {
        ApiErrorAssert.assertThat(api.users().deleteRaw(user, user.userId()), ResponseSpec.forbidden403())
                .hasDetail(SUPERADMIN_CANNOT_DELETE_OWN_ACCOUNT_MESSAGE);
    }

    @Test
    void rejectDeleteWithInvalidUUIDFormat(
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
