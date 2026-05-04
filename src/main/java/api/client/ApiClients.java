package api.client;

import api.transport.ApiRequester;

public final class ApiClients {
    private final ApiRequester apiRequester;

    private final AuthClient auth;
    private final UserClient users;
    private final SupplierClient suppliers;

    private ApiClients(ApiRequester apiRequester) {
        this.apiRequester = apiRequester;
        this.auth = new AuthClient(apiRequester);
        this.users = new UserClient(apiRequester);
        this.suppliers = new SupplierClient(apiRequester);
    }

    public static ApiClients create() {
        return new ApiClients(new ApiRequester());
    }

    public AuthClient auth() {
        return auth;
    }

    public UserClient users() {
        return users;
    }

    public SupplierClient suppliers() {
        return suppliers;
    }

}
