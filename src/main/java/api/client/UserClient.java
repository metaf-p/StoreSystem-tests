package api.client;

import api.endpoint.AuthEndpoints;
import api.spec.AuthServiceRequestSpecs;
import api.spec.ResponseSpec;
import api.transport.ApiRequest;
import api.transport.ApiRequester;
import io.restassured.response.Response;
import model.auth.common.AuthContext;
import model.auth.request.EditUserRequest;
import model.auth.request.PromoteRequest;
import model.auth.response.*;

import java.util.Map;
import java.util.UUID;

public class UserClient extends BaseApiClient {

    public UserClient(ApiRequester apiRequester) {
        super(apiRequester);
    }

    public CurrentUserResponse profile(AuthContext user) {
        return execute(
                ApiRequest.withoutBody(AuthEndpoints.ME),
                AuthServiceRequestSpecs.authenticatedRequest(user),
                ResponseSpec.ok200()
        );
    }

    public Response profileRaw(AuthContext user) {
        return executeRaw(
                ApiRequest.withoutBody(AuthEndpoints.ME),
                AuthServiceRequestSpecs.authenticatedRequest(user)
        );
    }

    public UserResponse getAll(AuthContext authContext, int page, int size) {
        return execute(
                ApiRequest.withQueryParams(
                        AuthEndpoints.USERS,
                        Map.of("page", page, "page_size", size)
                ),
                AuthServiceRequestSpecs.authenticatedRequest(authContext),
                ResponseSpec.ok200()
        );
    }

    public Response getAllRaw(AuthContext authContext) {
        return executeRaw(
                ApiRequest.withoutBody(AuthEndpoints.USERS),
                AuthServiceRequestSpecs.authenticatedRequest(authContext)
        );
    }

    public Response getAllWithoutAuthRaw() {
        return executeRaw(
                ApiRequest.withoutBody(AuthEndpoints.USERS),
                AuthServiceRequestSpecs.baseRequest()
        );
    }

    public PromoteResponse promote(
            AuthContext authContext,
            UUID userId,
            PromoteRequest body
    ) {
        return execute(
                ApiRequest.withBodyAndPathParams(AuthEndpoints.USERS_PROMOTE_ID, Map.of("userId", userId), body),
                AuthServiceRequestSpecs.authenticatedRequest(authContext),
                ResponseSpec.ok200()
        );
    }

    public Response promoteRaw(
            AuthContext promoter,
            UUID promotedUserId,
            PromoteRequest body
    ) {
        return executeRaw(
                ApiRequest.withBodyAndPathParams(
                        AuthEndpoints.USERS_PROMOTE_ID,
                        Map.of("userId", promotedUserId),
                        body
                ),
                AuthServiceRequestSpecs.authenticatedRequest(promoter)
        );
    }

    public EditUserResponse edit(
            AuthContext authContext,
            EditUserRequest body,
            UUID userId
    ) {
        return execute(
                ApiRequest.withBodyAndPathParams(AuthEndpoints.USERS_EDIT_ID, Map.of("userId", userId), body),
                AuthServiceRequestSpecs.authenticatedRequest(authContext),
                ResponseSpec.ok200()
        );
    }

    public Response editRaw(
            AuthContext editor,
            EditUserRequest body,
            UUID userId
    ) {
        return executeRaw(
                ApiRequest.withBodyAndPathParams(AuthEndpoints.USERS_EDIT_ID, Map.of("userId", userId), body),
                AuthServiceRequestSpecs.authenticatedRequest(editor)
        );
    }

    public DeleteUserResponse delete(
            AuthContext authContext,
            UUID userId
    ) {
        return execute(
                ApiRequest.withPathParams(AuthEndpoints.USERS_DELETE_ID, Map.of("userId", userId)),
                AuthServiceRequestSpecs.authenticatedRequest(authContext),
                ResponseSpec.ok200()
        );
    }

    public void deleteQuietly(
            AuthContext authContext,
            UUID userId
    ) {
        Response response = executeRaw(
                ApiRequest.withPathParams(AuthEndpoints.USERS_DELETE_ID, Map.of("userId", userId)),
                AuthServiceRequestSpecs.authenticatedRequest(authContext)
        );

        response.then().spec(ResponseSpec.deleteQuietly());
    }

    public Response deleteRaw(
            AuthContext deletingUser,
            UUID userId
    ) {
        return executeRaw(
                ApiRequest.withPathParams(AuthEndpoints.USERS_DELETE_ID, Map.of("userId", userId)),
                AuthServiceRequestSpecs.authenticatedRequest(deletingUser)
        );
    }

    public Response deleteRaw(
            AuthContext admin,
            String userId
    ) {
        return executeRaw(
                ApiRequest.withPathParams(AuthEndpoints.USERS_DELETE_ID, Map.of("userId", userId)),
                AuthServiceRequestSpecs.authenticatedRequest(admin)
        );
    }
}
