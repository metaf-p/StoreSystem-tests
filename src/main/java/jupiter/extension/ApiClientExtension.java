package jupiter.extension;

import api.client.ApiClients;
import api.client.UserClient;
import auth.AdminAuthBootstrap;
import data.auth.AuthUserFixture;
import data.auth.UserCleanup;
import jupiter.annotation.Admin;
import model.auth.common.AuthContext;
import org.junit.jupiter.api.extension.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ApiClientExtension implements ParameterResolver, AfterEachCallback {
    public static final ExtensionContext.Namespace NAMESPACE
            = ExtensionContext.Namespace.create(ApiClientExtension.class);

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {

        Class<?> parameterType = parameterContext.getParameter().getType();
        return parameterContext.isAnnotated(Admin.class)
                || parameterType.equals(AuthContext.class)
                || parameterType.equals(UserCleanup.class)
                || parameterType.equals(AuthUserFixture.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {

        ApiTestContext apiTestContext = getOrCreateContext(extensionContext);
        Class<?> parameterType = parameterContext.getParameter().getType();

        if (parameterContext.isAnnotated(Admin.class)
                && !parameterType.equals(AuthContext.class)) {
            throw new ExtensionConfigurationException("Only AuthContext class can be annotated with @Admin");
        }

        if (parameterContext.isAnnotated(Admin.class)
                && parameterType.equals(AuthContext.class)) {
            return apiTestContext.admin();
        }

        if (parameterType.equals(AuthUserFixture.class)) {
            return apiTestContext.authUserFixture();
        }

        if (parameterType.equals(AuthContext.class)) {
            UserCleanup userCleanup = new UserCleanup(apiTestContext.users);

            return apiTestContext.authUserFixture().createUser(userCleanup);
        }

        if (parameterType.equals(UserCleanup.class)) {
            return new UserCleanup(apiTestContext.users);
        }

        throw new ExtensionConfigurationException(
                "no client found for "
                        + parameterContext.getParameter().getType()
                        + " method parameter"
        );
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        ApiTestContext apiTestContext = getOrCreateContext(context);

        if (apiTestContext.users.isEmpty()) {
            return;
        }

        deleteUsers(
                apiTestContext.users,
                apiTestContext.api.users(),
                apiTestContext.admin()
        );
    }

    private void deleteUsers(Set<UUID> users, UserClient userClient, AuthContext admin) {
        for (UUID user : users) {
            userClient.deleteQuietly(admin, user);
        }
    }

    private ApiTestContext getOrCreateContext(ExtensionContext extensionContext) {
        return extensionContext.getStore(NAMESPACE).getOrComputeIfAbsent(
                extensionContext.getUniqueId(),
                key -> new ApiTestContext(),
                ApiTestContext.class);
    }

    private static class ApiTestContext {
        private final ApiClients api = ApiClients.create();
        private AuthUserFixture authUserFixture;
        private AuthContext admin;
        private final Set<UUID> users = new HashSet<>();

        private AuthUserFixture authUserFixture() {
            if (authUserFixture == null) {
                authUserFixture = new AuthUserFixture(api.auth(), api.users());
            }

            return authUserFixture;
        }

        private AuthContext admin() {
            if (admin == null) {
                admin = AdminAuthBootstrap.authenticate(api.auth());
            }

            return admin;
        }
    }
}
