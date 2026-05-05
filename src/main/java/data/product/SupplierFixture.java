package data.product;

import api.client.SupplierClient;
import model.auth.common.AuthContext;
import model.product.request.SupplierCreateRequest;
import model.product.response.SupplierResponse;

public class SupplierFixture {
    private final SupplierClient supplierClient;
    private final AuthContext defaultActor;

    public SupplierFixture(SupplierClient supplierClient, AuthContext defaultActor) {
        this.supplierClient = supplierClient;
        this.defaultActor = defaultActor;
    }

    public CreatedSupplier create(AuthContext actor) {
        SupplierCreateRequest request = ProductTestData.uniqueSupplier();
        SupplierResponse response = supplierClient.create(actor, request);

        return new CreatedSupplier(request, response);
    }

    public CreatedSupplier create() {
        SupplierCreateRequest request = ProductTestData.uniqueSupplier();
        SupplierResponse response = supplierClient.create(defaultActor, request);

        return new CreatedSupplier(request, response);
    }

    public CreatedSupplier createWithAllFields(AuthContext actor) {
        SupplierCreateRequest request = ProductTestData.supplierWithAllFields();
        SupplierResponse response = supplierClient.create(actor, request);

        return new CreatedSupplier(request, response);
    }

    public CreatedSupplier createWithAllFields() {
        SupplierCreateRequest request = ProductTestData.supplierWithAllFields();
        SupplierResponse response = supplierClient.create(defaultActor, request);

        return new CreatedSupplier(request, response);
    }

    public record CreatedSupplier(
            SupplierCreateRequest request,
            SupplierResponse response
    ) {}
}
