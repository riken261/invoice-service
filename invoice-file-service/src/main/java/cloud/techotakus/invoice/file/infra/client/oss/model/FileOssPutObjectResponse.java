package cloud.techotakus.invoice.file.infra.client.oss.model;

import java.util.Map;

public record FileOssPutObjectResponse(
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
