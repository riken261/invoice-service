package cloud.techotakus.invoice.file.infra.client.oss.model;

public record FileOssPresignObjectRequest(
        String providerCode,
        String tenantId,
        String fileId,
        String bucket,
        String objectKey,
        Long expiresSeconds,
        String responseContentDisposition,
        String responseContentType,
        String purpose
) {
}
