package cloud.techotakus.invoice.core.infra.client.file;

public record CoreFileUploadRequest(
        String tenantId,
        String ownerUserId,
        String fileName,
        String mimeType,
        Long size,
        String sha256,
        String objectKey,
        String contentBase64,
        String uploadNonce
) {
}
