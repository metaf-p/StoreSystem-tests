package data.product;

import api.client.SupplierClient;
import api.logging.ApiLogContext;
import model.auth.common.AuthContext;
import model.product.request.SupplierCreateRequest;
import model.product.response.SupplierResponse;

public class SupplierFixture {
    private final SupplierClient supplierClient;
    private final AuthContext defaultActor;
    private final SupplierCleanup supplierCleanup;

    public SupplierFixture(SupplierClient supplierClient, AuthContext defaultActor, SupplierCleanup supplierCleanup) {
        this.supplierClient = supplierClient;
        this.defaultActor = defaultActor;
        this.supplierCleanup = supplierCleanup;
    }

    public CreatedSupplier create(AuthContext actor) {
        SupplierCreateRequest request = ProductTestData.uniqueSupplier();
        return ApiLogContext.asSetup(() -> {
            SupplierResponse response = supplierClient.create(actor, request);
            supplierCleanup.addSupplier(response.supplierId());

            return new CreatedSupplier(request, response);
        });
    }

    public CreatedSupplier create() {
        return create(defaultActor);
    }

    public CreatedSupplier createWithAllFields(AuthContext actor) {
        SupplierCreateRequest request = ProductTestData.supplierWithAllFields();
        return ApiLogContext.asSetup(() -> {
            SupplierResponse response = supplierClient.create(actor, request);
            supplierCleanup.addSupplier(response.supplierId());

            return new CreatedSupplier(request, response);
        });
    }

    public CreatedSupplier createWithAllFields() {
        return createWithAllFields(defaultActor);
    }

    public record CreatedSupplier(
            SupplierCreateRequest request,
            SupplierResponse response
    ) {}
}
