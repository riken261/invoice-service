package cloud.techotakus.invoice.integration.api.model;

import java.time.OffsetDateTime;
import java.util.Map;

public record OssGetObjectResponseModel(
        boolean success,
        boolean exists,
        String providerCode,
        String storageType,
        String bucket,
        String objectKey,
        String contentBase64,
        Long sizeBytes,
        String etag,
        String sha256,
        String contentType,
        OffsetDateTime lastModified,
        Map<String, String> metadata,
        Long latencyMs
) {
}
