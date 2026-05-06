package jupiter.extension;

import data.auth.AuthUserFixture;
import data.product.SupplierFixture;
import jupiter.annotation.Admin;
import model.auth.common.AuthContext;
import org.junit.jupiter.api.extension.*;

public class ApiFixtureExtension implements ParameterResolver, AfterEachCallback {
    public static final ExtensionContext.Namespace NAMESPACE
            = ExtensionContext.Namespace.create(ApiFixtureExtension.class);

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {

        Class<?> parameterType = parameterContext.getParameter().getType();
        return parameterContext.isAnnotated(Admin.class)
                || parameterType.equals(AuthUserFixture.class)
                || parameterType.equals(SupplierFixture.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> parameterType = parameterContext.getParameter().getType();

        if (parameterContext.isAnnotated(Admin.class)) {
            if (!parameterType.equals(AuthContext.class)) {
                throw new ExtensionConfigurationException("Only AuthContext can be annotated with @Admin");
            }

            return ApiTestRuntime.get(extensionContext).admin();
        }

        if (parameterType.equals(AuthUserFixture.class)) {
            return ApiTestRuntime.get(extensionContext).authUserFixture();
        }

        if(parameterType.equals(SupplierFixture.class)) {
            return ApiTestRuntime.get(extensionContext).supplierFixture();
        }

        throw new ExtensionConfigurationException(
                "no fixture found for "
                        + parameterContext.getParameter().getType()
                        + " method parameter"
        );
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        ApiTestRuntime.get(context).cleanupSuppliers();
        ApiTestRuntime.get(context).cleanupUsers();
    }
}
