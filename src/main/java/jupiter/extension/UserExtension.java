package jupiter.extension;

import api.client.ApiClients;
import jupiter.annotation.CurrentUser;
import jupiter.annotation.TestUser;
import model.auth.common.AuthContext;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

public class UserExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserExtension.class);
    private final ApiClients api = ApiClients.create();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), TestUser.class)
                .ifPresent(
                        userAnno -> {
                            AuthContext user = ApiTestRuntime.get(context).createUser(userAnno.role());
                            setUser(context, user);
                        }
                );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(AuthContext.class)
                && parameterContext.isAnnotated(CurrentUser.class);
    }

    @Override
    public AuthContext resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return createdUser(extensionContext);
    }

    public static AuthContext createdUser(ExtensionContext context) {
        return context.getStore(NAMESPACE).get(context.getUniqueId(), AuthContext.class);
    }

    public static void setUser(ExtensionContext context, AuthContext user) {
        context.getStore(NAMESPACE).put(context.getUniqueId(), user);
    }
}
