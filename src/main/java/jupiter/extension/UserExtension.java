package jupiter.extension;

import api.client.ApiClients;
import data.auth.AuthTestData;
import jupiter.annotation.TestUser;
import model.auth.common.AuthContext;
import model.auth.common.UserRole;
import model.auth.request.RegisterUserRequest;
import model.auth.response.RegisterUserResponse;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

public class UserExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserExtension.class);
    private final ApiClients api = ApiClients.create();


    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), TestUser.class)
                .ifPresent(
                        userAnno -> {
                            if(UserRole.CUSTOMER.equals(userAnno.role())) {
                                RegisterUserRequest request = AuthTestData.uniqueUser();
                                RegisterUserResponse register = api.auth().register(request);
                            }
                        }
                );
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {

    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(AuthContext.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return null;
    }
}
