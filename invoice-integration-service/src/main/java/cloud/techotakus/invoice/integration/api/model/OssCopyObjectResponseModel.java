package cloud.techotakus.invoice.integration.api.model;

public record OssCopyObjectResponseModel(
        boolean success,
        String providerCode,
        String storageType,
        String sourceBucket,
        String sourceObjectKey,
        String targetBucket,
        String targetObjectKey,
        String etag,
        Long sizeBytes,
        Long latencyMs
) {
}
