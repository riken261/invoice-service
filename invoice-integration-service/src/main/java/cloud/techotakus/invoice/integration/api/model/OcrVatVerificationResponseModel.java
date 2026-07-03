package cloud.techotakus.invoice.integration.api.model;

import java.util.Map;

public record OcrVatVerificationResponseModel(
        String providerCode,
        String providerRequestId,
        String status,
        Boolean verified,
        Boolean matched,
        Boolean confidenceBoosted,
        Double confidenceDelta,
        String errorCode,
        String errorMessage,
        Map<String, Object> verifiedInvoice,
        Map<String, Object> rawResult
) {
}
