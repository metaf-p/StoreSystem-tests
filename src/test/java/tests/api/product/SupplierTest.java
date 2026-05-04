package tests.api.product;

import api.client.ApiClients;
import data.auth.AuthUserFixture;
import data.product.ProductTestData;
import jupiter.annotation.Admin;
import jupiter.annotation.ApiTest;
import model.auth.common.AuthContext;
import model.auth.common.UserRole;
import model.product.request.SupplierCreateRequest;
import model.product.response.SupplierResponse;
import org.junit.jupiter.api.Test;

@ApiTest
public class SupplierTest {
    private final ApiClients api = ApiClients.create();

    @Test
    void shouldCreateSupplierWithOperatorRole(
            @Admin AuthContext admin,
            AuthUserFixture authUserFixture
    ) {
        SupplierCreateRequest request = ProductTestData.uniqueSupplier();
        AuthContext operator = authUserFixture.createUserWithRole(UserRole.OPERATOR, admin);
        SupplierResponse supplierResponse = api.suppliers().create(operator, request);
    }
}
