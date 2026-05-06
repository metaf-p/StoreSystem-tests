package api.endpoint;

import io.restassured.common.mapper.TypeRef;
import model.common.MessageResponse;
import model.product.response.SupplierDocumentResponse;
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
    public static final Endpoint<SupplierResponse> SUPPLIER_BY_ID =
            new Endpoint<>(
                    "/suppliers/{supplier_id}",
                    HttpMethods.GET,
                    new TypeRef<SupplierResponse>() {
                    }
            );
    public static final Endpoint<SupplierResponse> SUPPLIERS_CREATE =
            new Endpoint<>(
                    "/suppliers/",
                    HttpMethods.POST,
                    new TypeRef<SupplierResponse>() {
                    }
            );
    public static final Endpoint<MessageResponse> SUPPLIERS_DELETE =
            new Endpoint<>(
                    "/suppliers/{supplier_id}",
                    HttpMethods.DELETE,
                    new TypeRef<MessageResponse>() {
                    }
            );
    public static final Endpoint<SupplierDocumentResponse> SUPPLIER_DOCUMENT_UPLOAD =
            new Endpoint<>(
                    "/suppliers/{supplier_id}/documents",
                    HttpMethods.POST,
                    new TypeRef<SupplierDocumentResponse>() {
                    }
            );

}
