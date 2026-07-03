package cloud.techotakus.invoice.integration.api.model;

public record OssDeleteObjectResponseModel(
        boolean success,
        String providerCode,
        String storageType,
        String bucket,
        String objectKey,
        boolean existed,
        Long latencyMs
) {
}
