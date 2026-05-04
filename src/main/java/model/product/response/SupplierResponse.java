package model.product.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record SupplierResponse(
        @JsonProperty("supplier_id")
        UUID supplierId,
        @JsonProperty("name")
        String name,
        @JsonProperty("contact_name")
        String contactName,
        @JsonProperty("contact_email")
        String contactEmail,
        @JsonProperty("phone_number")
        String phoneNumber,
        @JsonProperty("address")
        String address,
        @JsonProperty("country")
        String country,
        @JsonProperty("city")
        String city,
        @JsonProperty("website")
        String website
) {
}
