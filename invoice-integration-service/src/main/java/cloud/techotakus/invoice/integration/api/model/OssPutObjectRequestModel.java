package cloud.techotakus.invoice.integration.api.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record OssPutObjectRequestModel(
        String providerCode,
        @NotBlank String tenantId,
        @NotBlank String fileId,
        String bucket,
        @NotBlank String objectKey,
        String contentType,
        @NotBlank String contentBase64,
        @Min(0) Long sizeBytes,
        String sha256,
        Map<String, String> metadata,
        Map<String, String> tags
) {
}
