package tests.api.product;

import api.assertion.ApiErrorAssert;
import api.client.ApiClients;
import api.spec.ResponseSpec;
import data.product.ProductTestData;
import jupiter.annotation.Admin;
import jupiter.annotation.CurrentUser;
import jupiter.annotation.TestUser;
import jupiter.annotation.meta.ApiTest;
import model.auth.common.AuthContext;
import model.auth.common.UserRole;
import model.product.request.SupplierCreateRequest;
import model.product.response.SupplierResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
public class SupplierTest {
    private static final String DUPLICATE_SUPPLIER_ERROR_MESSAGE = "This supplier is already existed";
    private static final String FIELD_3_CHAR_MIN_LENGTH_ERROR_MESSAGE = "ensure this value has at least 3 characters";
    private static final String FIELD_MIN_LENGTH_ERROR_TYPE = "value_error.any_str.min_length";
    private static final String MISSING_REQUIRED_FIELD_ERROR_MESSAGE = "field required";
    private static final String MISSING_REQUIRED_FIELD_ERROR_TYPE = "value_error.missing";
    private static final String BLANK_CONTACT_NAME_ERROR_MESSAGE = "Contact name cannot be empty or contain only spaces.";
    private static final String VALUE_ERROR_TYPE = "value_error";
    private static final String INVALID_EMAIL_MESSAGE = "value is not a valid email address";
    private static final String VALUE_ERROR_EMAIL = "value_error.email";
    private static final String REQUIRES_OPERATOR_ACCESS_ERROR_MESSAGE = "Requires operator access";

    private final ApiClients api = ApiClients.create();

    public static Stream<Arguments> suppliersWithoutRequiredFields() {
        SupplierCreateRequest supplier = ProductTestData.uniqueSupplier();
        return Stream.of(
                Arguments.of("name", new SupplierCreateRequest(
                        null,
                        supplier.contactName(),
                        supplier.contactEmail(),
                        supplier.phoneNumber(),
                        supplier.address(),
                        supplier.country(),
                        supplier.city(),
                        supplier.website()
                )),
                Arguments.of("contact_name", new SupplierCreateRequest(
                        supplier.name(),
                        null,
                        supplier.contactEmail(),
                        supplier.phoneNumber(),
                        supplier.address(),
                        supplier.country(),
                        supplier.city(),
                        supplier.website()
                )),
                Arguments.of("contact_email", new SupplierCreateRequest(
                        supplier.name(),
                        supplier.contactName(),
                        null,
                        supplier.phoneNumber(),
                        supplier.address(),
                        supplier.country(),
                        supplier.city(),
                        supplier.website()
                ))
        );
    }

    @TestUser(role = UserRole.OPERATOR)
    @Test
    void shouldCreateSupplierWithOperatorRole(@CurrentUser AuthContext operator) {
        SupplierCreateRequest request = ProductTestData.uniqueSupplier();
        SupplierResponse supplierResponse = api.suppliers().create(operator, request);

        assertThat(supplierResponse.supplierId()).isNotNull();
    }

    @TestUser(role = UserRole.OPERATOR)
    @Test
    void shouldCreateSupplierWithAllFields(@CurrentUser AuthContext operator) {
        SupplierCreateRequest request = ProductTestData.supplierWithAllFields();
        SupplierResponse supplierResponse = api.suppliers().create(operator, request);

        assertThat(supplierResponse.supplierId()).isNotNull();
    }

    @TestUser(role = UserRole.ADMIN)
    @Test
    void shouldCreateSupplierWithAdminRole(@CurrentUser AuthContext admin) {
        SupplierCreateRequest request = ProductTestData.uniqueSupplier();
        SupplierResponse supplierResponse = api.suppliers().create(admin, request);

        assertThat(supplierResponse.supplierId()).isNotNull();
    }

    @TestUser
    @Test
    void forbidCreateSupplierWithCustomerRole(@CurrentUser AuthContext customer) {
        SupplierCreateRequest request = ProductTestData.uniqueSupplier();

        ApiErrorAssert.assertThat(api.suppliers().createRaw(customer, request), ResponseSpec.forbidden403())
                .hasDetail(REQUIRES_OPERATOR_ACCESS_ERROR_MESSAGE);
    }

    @TestUser(role = UserRole.OPERATOR)
    @Test
    void rejectsCreateSupplierWithDuplicateName(@CurrentUser AuthContext operator) {
        SupplierCreateRequest request = ProductTestData.uniqueSupplier();
        api.suppliers().create(operator, request);
        SupplierCreateRequest duplicateNameRequest = ProductTestData.supplierWithName(request.name());

        ApiErrorAssert.assertThat(
                api.suppliers().createRaw(operator, duplicateNameRequest),
                ResponseSpec.unprocessableEntity422()
        ).hasDetail(DUPLICATE_SUPPLIER_ERROR_MESSAGE);
    }

    @TestUser(role = UserRole.OPERATOR)
    @Test
    void rejectCreateSupplierWithBlankName(@CurrentUser AuthContext operator) {
        SupplierCreateRequest request = ProductTestData.supplierWithName("");

        ApiErrorAssert.assertThat(
                        api.suppliers().createRaw(operator, request),
                        ResponseSpec.unprocessableEntity422())
                .hasFirstValidationMessage(FIELD_3_CHAR_MIN_LENGTH_ERROR_MESSAGE)
                .hasFirstValidationType(FIELD_MIN_LENGTH_ERROR_TYPE);
    }

    @TestUser(role = UserRole.OPERATOR)
    @Test
    void rejectCreateSupplierWithBlankContactName(@CurrentUser AuthContext operator) {
        SupplierCreateRequest request = ProductTestData.supplierWithContactName("");

        ApiErrorAssert.assertThat(
                        api.suppliers().createRaw(operator, request),
                        ResponseSpec.unprocessableEntity422())
                .hasFirstValidationMessage(BLANK_CONTACT_NAME_ERROR_MESSAGE)
                .hasFirstValidationType(VALUE_ERROR_TYPE);
    }

    @TestUser(role = UserRole.OPERATOR)
    @Test
    void rejectCreateSupplierWithBlankContactEmail(@CurrentUser AuthContext operator) {
        SupplierCreateRequest request = ProductTestData.supplierWithContactEmail("");

        ApiErrorAssert.assertThat(
                api.suppliers().createRaw(operator, request),
                ResponseSpec.unprocessableEntity422())
                .hasFirstValidationType(VALUE_ERROR_EMAIL)
                .hasFirstValidationMessage(INVALID_EMAIL_MESSAGE);
    }

    @ParameterizedTest(name = "Without {0}")
    @MethodSource("suppliersWithoutRequiredFields")
    @TestUser(role = UserRole.OPERATOR)
    void rejectCreateSupplierWithoutRequiredFields(
            String fieldName,
            SupplierCreateRequest request,
            @CurrentUser AuthContext operator
    ) {
        ApiErrorAssert.assertThat(
                        api.suppliers().createRaw(operator, request),
                        ResponseSpec.unprocessableEntity422())
                .hasFirstValidationType(MISSING_REQUIRED_FIELD_ERROR_TYPE)
                .hasFirstValidationMessage(MISSING_REQUIRED_FIELD_ERROR_MESSAGE);
    }

    @TestUser
    @Test
    void shouldReturnAllSuppliersForCustomer(
            @CurrentUser AuthContext customer,
            @Admin AuthContext admin
    ) {
        SupplierCreateRequest request = ProductTestData.uniqueSupplier();
        api.suppliers().create(admin, request);
        List<SupplierResponse> allSuppliers = api.suppliers().getAll(customer);
        assertThat(allSuppliers).hasSizeGreaterThan(0);
    }

    @TestUser(role = UserRole.OPERATOR)
    @Test
    void shouldReturnAllSuppliersForOperator(
            @CurrentUser AuthContext operator
    ) {
        SupplierCreateRequest request = ProductTestData.uniqueSupplier();
        api.suppliers().create(operator, request);
        List<SupplierResponse> allSuppliers = api.suppliers().getAll(operator);
        assertThat(allSuppliers).hasSizeGreaterThan(0);
    }

    @TestUser(role = UserRole.ADMIN)
    @Test
    void shouldReturnAllSuppliersForAdmin(
            @CurrentUser AuthContext admin
    ) {
        SupplierCreateRequest request = ProductTestData.uniqueSupplier();
        api.suppliers().create(admin, request);
        List<SupplierResponse> allSuppliers = api.suppliers().getAll(admin);
        assertThat(allSuppliers).hasSizeGreaterThan(0);
    }
}
