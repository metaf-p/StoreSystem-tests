package tests.api.product;

import api.assertion.ApiErrorAssert;
import api.client.ApiClients;
import api.spec.ResponseSpec;
import jupiter.annotation.Admin;
import model.common.MessageResponse;
import model.product.request.SupplierDocumentType;
import data.product.SupplierFixture;
import jupiter.annotation.CurrentUser;
import jupiter.annotation.TestUser;
import jupiter.annotation.meta.ApiTest;
import model.auth.common.AuthContext;
import model.auth.common.UserRole;
import model.product.response.SupplierDocumentResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
public class SupplierDocumentTest {
    private static final String REQUIRES_OPERATOR_ACCESS_ERROR_MESSAGE = "Requires operator access";
    private static final String INVALID_FILE_FORMAT_ERROR_MESSAGE = "Invalid file format. Allowed types: pdf, docx, png, jpg, jpeg, xls, xlsx.";
    private static final String DOCUMENT_DELETED_MESSAGE = "Document deleted";

    private static final Path SAMPLE_PDF = Path.of("src/test/resources/documents/sample.pdf");

    private final ApiClients api = ApiClients.create();

    @TestUser(role = UserRole.OPERATOR)
    @Test
    void shouldUploadPdfFileSuccessfully(
            @CurrentUser AuthContext operator,
            SupplierFixture supplierFixture
    ) throws IOException {
        UUID supplierId = createSupplier(supplierFixture);

        SupplierDocumentResponse supplierDocumentResponse = api.suppliers()
                .uploadDocument(operator, supplierId, SupplierDocumentType.CONTRACT, "", SAMPLE_PDF, "application/pdf");

        assertThat(supplierDocumentResponse.documentId()).isNotNull();
        assertThat(supplierDocumentResponse.supplierId()).isEqualTo(supplierId);
        assertThat(supplierDocumentResponse.documentType()).isEqualTo(SupplierDocumentType.CONTRACT.value());
        assertThat(supplierDocumentResponse.originalFilename()).isEqualTo(SAMPLE_PDF.getFileName().toString());
        assertThat(supplierDocumentResponse.contentType()).isEqualTo("application/pdf");
        assertThat(supplierDocumentResponse.fileSize()).isEqualTo(Files.size(SAMPLE_PDF));
        assertThat(supplierDocumentResponse.uploadedBy()).isEqualTo(operator.userId().toString());
        assertThat(supplierDocumentResponse.createdAt()).isNotBlank();
    }

    @TestUser(role = UserRole.CUSTOMER)
    @Test
    void forbidUploadFileForCustomer(
            @CurrentUser AuthContext customer,
            SupplierFixture supplierFixture
    ) {
        UUID supplierId = createSupplier(supplierFixture);

        ApiErrorAssert.assertThat(
                api.suppliers().uploadDocumentRaw(customer, supplierId, SupplierDocumentType.CONTRACT, "", SAMPLE_PDF, "application/pdf"),
                ResponseSpec.forbidden403()
        ).hasDetail(REQUIRES_OPERATOR_ACCESS_ERROR_MESSAGE);
    }

    @TestUser(role = UserRole.ADMIN)
    @Test
    void rejectUploadInvalidTypeFile(
            @CurrentUser AuthContext admin,
            SupplierFixture supplierFixture
    ) {
        UUID supplierId = createSupplier(supplierFixture);
        Path document = Path.of("src/test/resources/documents/sample1.txt");

        ApiErrorAssert.assertThat(
                api.suppliers().uploadDocumentRaw(admin, supplierId, SupplierDocumentType.CONTRACT, "", document, "text/plain"),
                ResponseSpec.badRequest400()
        ).hasDetail(INVALID_FILE_FORMAT_ERROR_MESSAGE);
    }

    @TestUser(role = UserRole.CUSTOMER)
    @Test
    void getAllDocumentsOfSupplier(
            @CurrentUser AuthContext customer,
            @Admin AuthContext admin,
            SupplierFixture supplierFixture
    ) {
        UUID supplierId = createSupplier(supplierFixture);
        SupplierDocumentResponse supplierDocumentResponse = uploadSampleDocument(admin, supplierId);

        List<SupplierDocumentResponse> allDocuments = api.suppliers().getAllDocuments(customer, supplierId);

        assertThat(allDocuments)
                .filteredOn(doc -> doc.documentId().equals(supplierDocumentResponse.documentId()))
                .singleElement()
                .usingRecursiveComparison()
                .isEqualTo(supplierDocumentResponse);
    }

    @TestUser(role = UserRole.OPERATOR)
    @Test
    void shouldDeleteSupplierDocument(
            @CurrentUser AuthContext operator,
            SupplierFixture supplierFixture
    ) {
        UUID supplierId = createSupplier(supplierFixture);
        SupplierDocumentResponse supplierDocumentResponse = uploadSampleDocument(operator, supplierId);

        MessageResponse messageResponse = api.suppliers().deleteDocument(operator, supplierId, supplierDocumentResponse.documentId());
        assertThat(messageResponse.message()).isEqualTo(DOCUMENT_DELETED_MESSAGE);

        List<SupplierDocumentResponse> allDocuments = api.suppliers().getAllDocuments(operator, supplierId);
        assertThat(allDocuments).extracting(SupplierDocumentResponse::documentId)
                .doesNotContain(supplierDocumentResponse.documentId());
    }

    private UUID createSupplier(SupplierFixture supplierFixture) {
        return supplierFixture.create().response().supplierId();
    }

    private SupplierDocumentResponse uploadSampleDocument(AuthContext actor, UUID supplierId) {
        return api.suppliers()
                .uploadDocument(
                        actor,
                        supplierId,
                        SupplierDocumentType.CERTIFICATE,
                        "",
                        SAMPLE_PDF,
                        "application/pdf"
                );
    }
}
