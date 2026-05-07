package tests.api.auth;

import api.assertion.ApiErrorAssert;
import api.client.ApiClients;
import api.spec.ResponseSpec;
import data.auth.AuthUserFixture;
import io.restassured.response.Response;
import jupiter.annotation.Admin;
import jupiter.annotation.CurrentUser;
import jupiter.annotation.TestUser;
import jupiter.annotation.meta.ApiTest;
import model.auth.common.AuthContext;
import model.auth.common.UserRole;
import model.auth.request.PromoteRequest;
import model.auth.response.CurrentUserResponse;
import model.auth.response.PromoteResponse;
import model.auth.response.UserResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
public class UsersTest {
    private final ApiClients api = ApiClients.create();

    private static final String NOT_AUTH_MESSAGE = "Not authenticated";
    private static final String TOKEN_INVALID_MESSAGE = "Invalid token";
    private static final String SUCCESSFUL_PROMOTE_MESSAGE = "User role successfully updated";
    private static final String USER_ALREADY_HAS_ROLE_MESSAGE = "User already has this role";
    private static final String INSUFFICIENT_RIGHTS_MESSAGE = "Insufficient rights";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    @TestUser
    @Test
    void viewOwnProfileForAuthenticatedUser(@CurrentUser AuthContext authContext) {
        CurrentUserResponse profile = api.users().profile(authContext);
        assertThat(profile.id()).isEqualTo(authContext.userId());
    }

    @TestUser
    @Test
    void shouldReturnNotFoundWhenRequestProfileOfDeletedUser(
            @CurrentUser AuthContext user,
            @Admin AuthContext admin
    ) {
        api.users().delete(admin, user.userId());

        ApiErrorAssert.assertThat(api.users().profileRaw(user), ResponseSpec.notFound404())
                .hasDetail(USER_NOT_FOUND_MESSAGE);
    }

    @Test
    void checkUsersPaginationOnNonEmptyList(
            AuthUserFixture authUserFixture,
            @Admin AuthContext admin
    ) {
        int usersCount = 8;
        int page = 2;
        int pageSize = 2;

        authUserFixture.registerUsers(usersCount);

        UserResponse userResponse = api.users().getAll(admin, page, pageSize);
        assertThat(userResponse.page()).isEqualTo(page);
        assertThat(userResponse.pageSize()).isEqualTo(pageSize);
        assertThat(userResponse.users()).hasSizeLessThanOrEqualTo(pageSize);
        assertThat(userResponse.total()).isGreaterThanOrEqualTo(usersCount);
        assertThat(userResponse.totalPages()).isEqualTo(
                (int) Math.ceil((double) userResponse.total() / pageSize)
        );
    }

    @Test
    void rejectUsersListRequestWithoutToken() {
        ApiErrorAssert.assertThat(
                        api.users().getAllWithoutAuthRaw(),
                        ResponseSpec.forbidden403())
                .hasDetail(NOT_AUTH_MESSAGE);
    }

    @Test
    void rejectUsersListRequestWithInvalidToken() {
        AuthContext authContext = new AuthContext(
                null,
                "invalid-token",
                null,
                "bearer"
        );
        ApiErrorAssert.assertThat(
                        api.users().getAllRaw(authContext),
                        ResponseSpec.unauthorized401())
                .hasDetail(TOKEN_INVALID_MESSAGE);
    }

    @TestUser(role = UserRole.ADMIN)
    @Test
    void shouldPromoteRegularUserToAdmin(
            @CurrentUser AuthContext admin,
            AuthUserFixture authUserFixture
    ) {
        AuthContext user = authUserFixture.createUser();
        PromoteRequest promoteRequest = new PromoteRequest(UserRole.ADMIN);

        PromoteResponse promoteResponse = api.users()
                .promote(admin, user.userId(), promoteRequest);
        assertThat(promoteResponse.detail())
                .isEqualTo(SUCCESSFUL_PROMOTE_MESSAGE);

        CurrentUserResponse profile = api.users().profile(user);
        assertThat(profile.role()).isEqualTo(UserRole.ADMIN);
    }

    @TestUser(role = UserRole.OPERATOR)
    @Test
    void rejectPromotionWithInsufficientRights(
            @CurrentUser AuthContext promoter,
            AuthUserFixture authUserFixture
    ) {
        AuthContext user = authUserFixture.createUser();
        PromoteRequest request = new PromoteRequest(UserRole.ADMIN);

        Response response = api.users()
                .promoteRaw(promoter, user.userId(), request);
        ApiErrorAssert.assertThat(response, ResponseSpec.forbidden403())
                .hasDetail(INSUFFICIENT_RIGHTS_MESSAGE);

        CurrentUserResponse profile = api.users().profile(user);
        assertThat(profile.role()).isEqualTo(UserRole.CUSTOMER);
    }

    @TestUser(role = UserRole.ADMIN)
    @Test
    void shouldDisplayAlreadyHasRoleMessageWhenPromoteToSameRole(
            @CurrentUser AuthContext user
    ) {
        PromoteRequest request = new PromoteRequest(UserRole.ADMIN);

        PromoteResponse promoteResponse = api.users().promote(user, user.userId(), request);
        assertThat(promoteResponse.detail())
                .isEqualTo(USER_ALREADY_HAS_ROLE_MESSAGE);

        CurrentUserResponse profile = api.users().profile(user);
        assertThat(profile.role()).isEqualTo(UserRole.ADMIN);
    }
}
