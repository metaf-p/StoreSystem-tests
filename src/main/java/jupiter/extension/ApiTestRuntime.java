package jupiter.extension;

import api.client.ApiClients;
import auth.AdminAuthBootstrap;
import data.auth.AuthUserFixture;
import model.auth.common.AuthContext;
import model.auth.common.UserRole;
import org.junit.jupiter.api.extension.ExtensionContext;

final class ApiTestRuntime {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(ApiTestRuntime.class);

    private final ApiClients api = ApiClients.create();

    private AuthUserFixture authUserFixture;
    private AuthContext admin;

    static ApiTestRuntime get(ExtensionContext context) {
        return context.getStore(NAMESPACE).getOrComputeIfAbsent(
                ApiTestRuntime.class,
                key -> new ApiTestRuntime(),
                ApiTestRuntime.class
        );
    }

    AuthContext admin() {
        if(admin == null) {
            admin = AdminAuthBootstrap.authenticate(api.auth());
        }
        return admin;
    }

    AuthUserFixture authUserFixture() {
        if(authUserFixture == null) {
            authUserFixture = new AuthUserFixture(api.auth(), api.users());
        }
        return authUserFixture;
    }

    AuthContext createUser(UserRole role) {
        if(role == UserRole.CUSTOMER) {
            return authUserFixture().createUser();
        }

        return authUserFixture().createUserWithRole(role, admin());
    }
}
