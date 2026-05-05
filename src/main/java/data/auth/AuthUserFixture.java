package data.auth;

import api.client.AuthClient;
import api.client.UserClient;
import api.logging.ApiLogContext;
import model.auth.common.AuthContext;
import model.auth.common.UserRole;
import model.auth.request.LoginRequest;
import model.auth.request.PromoteRequest;
import model.auth.request.RegisterUserRequest;
import model.auth.response.RegisterUserResponse;

import java.util.ArrayList;
import java.util.List;

public final class AuthUserFixture {
    private final AuthClient authClient;
    private final UserClient userClient;

    public AuthUserFixture(AuthClient authClient, UserClient userClient) {
        this.authClient = authClient;
        this.userClient = userClient;
    }

    public AuthContext createUser() {
        return ApiLogContext.asSetup(() -> {
            RegisterUserRequest request = AuthTestData.uniqueUser();
            registerUser(request);

            LoginRequest loginRequest = new LoginRequest(
                    request.email(),
                    request.password()
            );

            return authClient.authenticate(loginRequest);
        });

    }

    public RegisterUserResponse registerUser(RegisterUserRequest request) {
        return ApiLogContext.asSetup(() -> authClient.register(request));
    }

    public List<RegisterUserResponse> registerUsers(int count) {
        return ApiLogContext.asSetup(() -> {
            List<RegisterUserResponse> users = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                RegisterUserRequest request = AuthTestData.uniqueUser();
                RegisterUserResponse response = registerUser(request);
                users.add(response);
            }

            return users;
        });
    }

    public AuthContext createUserWithRole(
            UserRole role,
            AuthContext admin
    ) {
        return ApiLogContext.asSetup(() -> {
            RegisterUserRequest registerUser = AuthTestData.uniqueUser();
            RegisterUserResponse registerUserResponse = registerUser(registerUser);

            if (role != UserRole.CUSTOMER) {
                userClient.promote(admin, registerUserResponse.user().userId(), new PromoteRequest(role));
            }

            return authClient.authenticate(
                    new LoginRequest(registerUser.email(), registerUser.password())
            );
        });
    }
}
