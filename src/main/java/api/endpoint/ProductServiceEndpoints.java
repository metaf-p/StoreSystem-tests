package api.endpoint;

import io.restassured.common.mapper.TypeRef;
import model.product.response.SupplierResponse;

import java.util.List;

public final class ProductServiceEndpoints {

    private ProductServiceEndpoints() {}

    public static final Endpoint<List<SupplierResponse>> SUPPLIERS_LIST =
            new Endpoint<>(
                    "/suppliers/",
                    HttpMethods.GET,
                    new TypeRef<List<SupplierResponse>>() {
                    }
            );
    public static final Endpoint<SupplierResponse> SUPPLIERS_CREATE =
            new Endpoint<>(
                    "/suppliers/",
                    HttpMethods.POST,
                    new TypeRef<SupplierResponse>() {
                    }
            );
}
