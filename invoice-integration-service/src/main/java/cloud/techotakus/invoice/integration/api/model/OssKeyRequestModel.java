package cloud.techotakus.invoice.integration.api.model;

import jakarta.validation.constraints.NotBlank;

public record OssKeyRequestModel(
        String providerCode,
        @NotBlank String tenantId,
        String fileId,
        String bucket,
        @NotBlank String objectKey
) {
}
