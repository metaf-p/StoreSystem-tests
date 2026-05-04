package model.product.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SupplierCreateRequest(
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
