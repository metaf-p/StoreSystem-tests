package model.product.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record SupplierDocumentResponse(
        @JsonProperty("document_id")
        UUID documentId,
        @JsonProperty("supplier_id")
        UUID supplierId,
        @JsonProperty("document_type")
        String documentType,
        @JsonProperty("original_filename")
        String originalFilename,
        @JsonProperty("content_type")
        String contentType,
        @JsonProperty("file_size")
        long fileSize,
        @JsonProperty("uploaded_by")
        String uploadedBy,
        @JsonProperty("created_at")
        String createdAt,
        @JsonProperty("description")
        String description
) {
}
