package cloud.techotakus.invoice.integration.api.model;

public record OssHealthRequestModel(
        String providerCode,
        String bucket,
        String testPrefix,
        Boolean writeTestEnabled
) {
}
