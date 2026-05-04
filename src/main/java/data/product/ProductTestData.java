package data.product;

import model.product.request.SupplierCreateRequest;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

public final class ProductTestData {
    private ProductTestData() {
    }

    public static SupplierCreateRequest uniqueSupplier() {
        String id = UUID.randomUUID().toString();
        String randomEmail = RandomStringUtils.secure().nextAlphanumeric(3, 20) + "@example.com";

        return new SupplierCreateRequest(
                "supplier" + id,
                "contact " + id,
                randomEmail,
                null,
                null,
                null,
                null,
                null
        );
    }
}
