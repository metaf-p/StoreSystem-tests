package jupiter.extension;

import api.client.ApiClients;
import api.logging.ApiLogContext;
import auth.AdminAuthBootstrap;
import data.auth.AuthUserFixture;
import data.auth.UserCleanup;
import data.product.SupplierCleanup;
import data.product.SupplierFixture;
import model.auth.common.AuthContext;
import model.auth.common.UserRole;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashSet;

final class ApiTestRuntime {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(ApiTestRuntime.class);

    private final ApiClients api = ApiClients.create();

    private AuthUserFixture authUserFixture;
    private SupplierFixture supplierFixture;
    private AuthContext admin;
    private final UserCleanup userCleanup = new UserCleanup(new HashSet<>());
    private final SupplierCleanup supplierCleanup = new SupplierCleanup(new HashSet<>());

    static ApiTestRuntime get(ExtensionContext context) {
        return context.getStore(NAMESPACE).getOrComputeIfAbsent(
                ApiTestRuntime.class,
                key -> new ApiTestRuntime(),
                ApiTestRuntime.class
        );
    }

    AuthContext admin() {
        if (admin == null) {
            admin = ApiLogContext.asSetup(
                    () -> AdminAuthBootstrap.authenticate(api.auth())
            );
        }
        return admin;
    }

    AuthUserFixture authUserFixture() {
        if (authUserFixture == null) {
            authUserFixture = new AuthUserFixture(api.auth(), api.users(), userCleanup);
        }
        return authUserFixture;
    }

    SupplierFixture supplierFixture() {
        if (supplierFixture == null) {
            supplierFixture = new SupplierFixture(api.suppliers(), admin(), supplierCleanup);
        }

        return supplierFixture;
    }

    AuthContext createUser(UserRole role) {
        if (role == UserRole.CUSTOMER) {
            return authUserFixture().createUser();
        }

        return authUserFixture().createUserWithRole(role, admin());
    }

    void cleanupUsers() {

        if (userCleanup.isEmpty()) {
            return;
        }
        ApiLogContext.asCleanup(() -> {
            try {
                userCleanup.createdUsers().forEach(
                        userId -> api.users().deleteQuietly(admin(), userId)
                );
            } finally {
                userCleanup.clear();
            }
        });
    }

    void cleanupSuppliers() {
        if (supplierCleanup.isEmpty()) {
            return;
        }

        ApiLogContext.asCleanup(() -> {
            try {
                supplierCleanup.createdSuppliers().forEach(
                        supplierId -> api.suppliers().deleteQuietly(admin(), supplierId)
                );
            } finally {
                supplierCleanup.clear();
            }
        });

    }
}
