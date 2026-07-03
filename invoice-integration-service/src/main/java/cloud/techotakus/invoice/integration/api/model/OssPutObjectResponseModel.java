package cloud.techotakus.invoice.integration.api.model;

import java.util.Map;

public record OssPutObjectResponseModel(
        boolean success,
        String providerCode,
        String storageType,
        String bucket,
        String objectKey,
        String etag,
        Long sizeBytes,
        String sha256,
        String contentType,
        Map<String, String> metadata,
        Long latencyMs
) {
}
