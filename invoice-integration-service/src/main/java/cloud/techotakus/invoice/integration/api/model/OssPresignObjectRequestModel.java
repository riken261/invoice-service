package cloud.techotakus.invoice.integration.api.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OssPresignObjectRequestModel(
        String providerCode,
        @NotBlank String tenantId,
        String fileId,
        String bucket,
        @NotBlank String objectKey,
        @Min(1) Long expiresSeconds,
        String responseContentDisposition,
        String responseContentType,
        String purpose
) {
}
