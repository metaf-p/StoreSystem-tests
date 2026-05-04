package auth;

import api.client.AuthClient;
import config.Config;
import model.auth.common.AuthContext;
import model.auth.request.LoginRequest;

public final class AdminAuthBootstrap {
    private AdminAuthBootstrap() {
    }

    public static AuthContext authenticate(AuthClient authClient) {
        LoginRequest loginRequest = new LoginRequest(
                Config.getInstance().adminLogin(),
                Config.getInstance().adminPassword()
        );

        return authClient.authenticate(loginRequest);
    }
}
