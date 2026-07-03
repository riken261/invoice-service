package cloud.techotakus.invoice.integration.api.model;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record OssCopyObjectRequestModel(
        String providerCode,
        @NotBlank String tenantId,
        String fileId,
        String sourceBucket,
        @NotBlank String sourceObjectKey,
        String targetBucket,
        @NotBlank String targetObjectKey,
        String metadataDirective,
        Map<String, String> metadata,
        Map<String, String> tags
) {
}
