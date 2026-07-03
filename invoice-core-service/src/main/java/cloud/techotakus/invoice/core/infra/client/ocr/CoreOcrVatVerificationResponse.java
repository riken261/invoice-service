package cloud.techotakus.invoice.core.infra.client.ocr;

import java.util.Map;

public record CoreOcrVatVerificationResponse(
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
