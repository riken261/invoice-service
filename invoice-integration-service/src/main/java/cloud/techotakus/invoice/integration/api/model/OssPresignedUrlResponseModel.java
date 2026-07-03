package cloud.techotakus.invoice.integration.api.model;

import java.time.OffsetDateTime;

public record OssPresignedUrlResponseModel(
        boolean success,
        String providerCode,
        String storageType,
        String bucket,
        String objectKey,
        String signedUrl,
        OffsetDateTime expiresAt,
        String method,
        String purpose,
        Long latencyMs
) {
}
