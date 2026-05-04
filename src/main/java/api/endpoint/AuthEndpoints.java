package api.endpoint;

import io.restassured.common.mapper.TypeRef;
import model.auth.response.*;

public final class AuthEndpoints {
    private AuthEndpoints() {
    }

    public static final Endpoint<LoginResponse> LOGIN =
            new Endpoint<>(
                    "/login",
                    HttpMethods.POST,
                    new TypeRef<LoginResponse>() {
                    }
            );
    public static final Endpoint<RegisterUserResponse> REGISTER =
            new Endpoint<>(
                    "/register",
                    HttpMethods.POST,
                    new TypeRef<RegisterUserResponse>() {
                    }
            );
    public static final Endpoint<CurrentUserResponse> ME =
            new Endpoint<>(
                    "/me",
                    HttpMethods.GET,
                    new TypeRef<CurrentUserResponse>() {
                    }
            );
    public static final Endpoint<UserResponse> USERS =
            new Endpoint<>(
                    "/users",
                    HttpMethods.GET,
                    new TypeRef<UserResponse>() {
                    }
            );
    public static final Endpoint<PromoteResponse> USERS_PROMOTE_ID =
            new Endpoint<>(
                    "/users/{userId}/role",
                    HttpMethods.PUT,
                    new TypeRef<PromoteResponse>() {
                    }
            );
    public static final Endpoint<EditUserResponse> USERS_EDIT_ID =
            new Endpoint<>(
                    "/users/edit/{userId}",
                    HttpMethods.PUT,
                    new TypeRef<EditUserResponse>() {
                    }
            );

    public static final Endpoint<DeleteUserResponse> USERS_DELETE_ID =
            new Endpoint<>(
                    "/users/delete/{userId}",
                    HttpMethods.DELETE,
                    new TypeRef<DeleteUserResponse>() {
                    }
            );
}
