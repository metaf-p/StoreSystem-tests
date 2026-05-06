package api.client;

import api.endpoint.ProductServiceEndpoints;
import api.spec.ProductServiceRequestSpec;
import api.spec.ResponseSpec;
import api.transport.ApiRequest;
import api.transport.ApiRequester;
import io.restassured.response.Response;
import model.auth.common.AuthContext;
import model.common.MessageResponse;
import model.product.request.SupplierCreateRequest;
import model.product.response.SupplierResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SupplierClient extends BaseApiClient {
    public SupplierClient(ApiRequester apiRequester) {
        super(apiRequester);
    }

    public List<SupplierResponse> getAll(AuthContext actor) {
        return execute(
                ApiRequest.withoutBody(ProductServiceEndpoints.SUPPLIERS_LIST),
                ProductServiceRequestSpec.authenticatedRequest(actor),
                ResponseSpec.ok200()
        );
    }

    public SupplierResponse get(AuthContext actor, UUID supplierId) {
        return execute(
                ApiRequest.withPathParams(
                        ProductServiceEndpoints.SUPPLIER_BY_ID,
                        Map.of("supplier_id", supplierId)
                ),
                ProductServiceRequestSpec.authenticatedRequest(actor),
                ResponseSpec.ok200()
        );
    }

    public Response getWithoutAuthRaw(UUID supplierId) {
        return executeRaw(
                ApiRequest.withPathParams(
                        ProductServiceEndpoints.SUPPLIER_BY_ID,
                        Map.of("supplier_id", supplierId)
                ),
                ProductServiceRequestSpec.unauthenticatedRequest()
        );
    }

    public SupplierResponse create(AuthContext actor, SupplierCreateRequest request) {
        return execute(
                ApiRequest.withBody(ProductServiceEndpoints.SUPPLIERS_CREATE, request),
                ProductServiceRequestSpec.authenticatedRequest(actor),
                ResponseSpec.created201()
        );
    }

    public Response createRaw(AuthContext actor, SupplierCreateRequest request) {
        return executeRaw(
                ApiRequest.withBody(ProductServiceEndpoints.SUPPLIERS_CREATE, request),
                ProductServiceRequestSpec.authenticatedRequest(actor)
        );
    }

    public Response createWithoutAuthRaw(SupplierCreateRequest request) {
        return executeRaw(
                ApiRequest.withBody(ProductServiceEndpoints.SUPPLIERS_CREATE, request),
                ProductServiceRequestSpec.unauthenticatedRequest()
        );
    }

    public MessageResponse delete(AuthContext actor, UUID supplierId) {
        return execute(
                ApiRequest.withPathParams(
                        ProductServiceEndpoints.SUPPLIERS_DELETE,
                        Map.of("supplier_id", supplierId)
                ),
                ProductServiceRequestSpec.authenticatedRequest(actor),
                ResponseSpec.ok200()
        );
    }

    public Response deleteRaw(AuthContext actor, UUID supplierId) {
        return executeRaw(
                ApiRequest.withPathParams(
                        ProductServiceEndpoints.SUPPLIERS_DELETE,
                        Map.of("supplier_id", supplierId)
                ),
                ProductServiceRequestSpec.authenticatedRequest(actor)
        );
    }

    public void deleteQuietly(AuthContext actor, UUID supplierId) {
        executeRaw(
                ApiRequest.withPathParams(
                        ProductServiceEndpoints.SUPPLIERS_DELETE,
                        Map.of("supplier_id", supplierId)
                ),
                ProductServiceRequestSpec.authenticatedRequest(actor)
        ).then().spec(ResponseSpec.deleteQuietly());
    }
}
