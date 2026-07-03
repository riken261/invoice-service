package cloud.techotakus.invoice.file.infra.client.oss.model;

import java.time.OffsetDateTime;

public record FileOssPresignedUrlResponse(
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
