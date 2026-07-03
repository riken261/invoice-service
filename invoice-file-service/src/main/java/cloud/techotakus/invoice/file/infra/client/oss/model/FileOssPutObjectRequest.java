package cloud.techotakus.invoice.file.infra.client.oss.model;

import java.util.Map;

public record FileOssPutObjectRequest(
        String providerCode,
        String tenantId,
        String fileId,
        String bucket,
        String objectKey,
        String contentType,
        String contentBase64,
        Long sizeBytes,
        String sha256,
        Map<String, String> metadata,
        Map<String, String> tags
) {
}
