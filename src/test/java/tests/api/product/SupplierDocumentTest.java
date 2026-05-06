package tests.api.product;

import api.client.ApiClients;
import model.product.request.SupplierDocumentType;
import data.product.SupplierFixture;
import jupiter.annotation.CurrentUser;
import jupiter.annotation.TestUser;
import jupiter.annotation.meta.ApiTest;
import model.auth.common.AuthContext;
import model.auth.common.UserRole;
import model.product.response.SupplierDocumentResponse;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
public class SupplierDocumentTest {
    private final ApiClients api = ApiClients.create();
    @TestUser(role = UserRole.OPERATOR)
    @Test
    void shouldUploadPdfFileSuccessfully(
            @CurrentUser AuthContext operator,
            SupplierFixture supplierFixture
    ) {
        UUID supplierId = supplierFixture.create().response().supplierId();
        Path document = Path.of("src/test/resources/documents/sample.pdf");

        SupplierDocumentResponse supplierDocumentResponse = api.suppliers()
                .uploadDocument(operator, supplierId, SupplierDocumentType.CONTRACT, "", document);

        assertThat(supplierDocumentResponse.documentId()).isNotNull();
        assertThat(supplierDocumentResponse.supplierId()).isEqualTo(supplierId);
        assertThat(supplierDocumentResponse.documentType()).isEqualTo(SupplierDocumentType.CONTRACT.value());
        assertThat(supplierDocumentResponse.originalFilename()).isEqualTo("sample.pdf");
        assertThat(supplierDocumentResponse.contentType()).isEqualTo("application/pdf");
        assertThat(supplierDocumentResponse.fileSize()).isGreaterThan(0);
    }
}
