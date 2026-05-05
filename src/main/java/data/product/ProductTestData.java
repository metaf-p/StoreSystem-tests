package data.product;

import model.product.request.SupplierCreateRequest;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

public final class ProductTestData {
    private ProductTestData() {
    }

    public static SupplierCreateRequest uniqueSupplier() {
        return new SupplierCreateRequest(
                supplierName(),
                contactName(),
                contactEmail(),
                null,
                null,
                null,
                null,
                null
        );
    }

    public static SupplierCreateRequest supplierWithAllFields() {
        return new SupplierCreateRequest(
                supplierName(),
                contactName(),
                contactEmail(),
                phoneNumber(),
                address(),
                country(),
                city(),
                website()
        );
    }

    public static SupplierCreateRequest supplierWithName(String supplierName) {
        SupplierCreateRequest supplier = uniqueSupplier();
        return new SupplierCreateRequest(
                supplierName,
                supplier.contactName(),
                supplier.contactEmail(),
                supplier.phoneNumber(),
                supplier.address(),
                supplier.country(),
                supplier.city(),
                supplier.website()
        );
    }

    public static SupplierCreateRequest supplierWithContactName(String contactName) {
        SupplierCreateRequest supplier = uniqueSupplier();
        return new SupplierCreateRequest(
                supplier.name(),
                contactName,
                supplier.contactEmail(),
                supplier.phoneNumber(),
                supplier.address(),
                supplier.country(),
                supplier.city(),
                supplier.website()
        );
    }

    public static SupplierCreateRequest supplierWithContactEmail(String contactEmail) {
        SupplierCreateRequest supplier = uniqueSupplier();
        return new SupplierCreateRequest(
                supplier.name(),
                supplier.contactName(),
                contactEmail,
                supplier.phoneNumber(),
                supplier.address(),
                supplier.country(),
                supplier.city(),
                supplier.website()
        );
    }

    private static String supplierName() {
        return "Supplier " + UUID.randomUUID();
    }

    private static String contactName() {
        return "Contact " + UUID.randomUUID();
    }

    private static String contactEmail() {
        return RandomStringUtils.secure().nextAlphanumeric(3, 20) + "@example.com";
    }

    private static String phoneNumber() {
        return "+" + RandomStringUtils.secure().nextNumeric(11, 15);
    }

    private static String address() {
        return "Test Address " + RandomStringUtils.secure().nextAlphanumeric(5, 100);
    }

    private static String country() {
        return RandomStringUtils.secure().nextAlphabetic(2, 51);
    }

    private static String city() {
        return RandomStringUtils.secure().nextAlphabetic(2, 50);
    }

    private static String website() {
        return "https://website" +UUID.randomUUID() + ".com";
    }
}
