package cloud.techotakus.invoice.integration.api.model;

import java.util.Map;

public record OssHealthResponseModel(
        boolean success,
        String providerCode,
        String storageType,
        String bucket,
        boolean bucketAccessible,
        boolean writeTestPassed,
        boolean presignSupported,
        Long latencyMs,
        String errorCode,
        String errorMessage,
        Map<String, Object> providerResult
) {
}
