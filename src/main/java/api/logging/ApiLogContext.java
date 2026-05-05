package api.logging;

import java.util.function.Supplier;

public class ApiLogContext {
    private static final ThreadLocal<ApiCallScope> CURRENT_SCOPE =
            ThreadLocal.withInitial(() -> ApiCallScope.TEST);

    public static ApiCallScope currentScope() {
        return CURRENT_SCOPE.get();
    }

    public static <T> T asSetup(Supplier<T> action) {
        return withScope(ApiCallScope.SETUP, action);
    }

    public static void asSetup(Runnable action) {
        withScope(ApiCallScope.SETUP, action);
    }

    public static <T> T asCleanup(Supplier<T> action) {
        return withScope(ApiCallScope.CLEANUP, action);
    }

    public static void asCleanup(Runnable action) {
        withScope(ApiCallScope.CLEANUP, action);
    }

    private static <T> T withScope(ApiCallScope scope, Supplier<T> action) {
        ApiCallScope previousScope = CURRENT_SCOPE.get();
        CURRENT_SCOPE.set(scope);

        try {
            return action.get();
        } finally {
            CURRENT_SCOPE.set(previousScope);
        }
    }

    private static void withScope(ApiCallScope scope, Runnable action) {
        ApiCallScope previousScope = CURRENT_SCOPE.get();
        CURRENT_SCOPE.set(scope);

        try {
            action.run();
        } finally {
            CURRENT_SCOPE.set(previousScope);
        }
    }
}
