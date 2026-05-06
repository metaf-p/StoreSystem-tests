package model.product.request;

public enum SupplierDocumentType {
    CONTRACT("contract"),
    CERTIFICATE("certificate"),
    REQUISITES("requisites"),
    PRICE_LIST("price_list"),
    OTHER("other");

    private final String value;

    SupplierDocumentType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
