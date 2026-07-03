package cloud.techotakus.invoice.core.infra.client.ocr;

import java.util.List;
import java.util.Map;

public record CoreOcrRecognizeResponse(
        boolean success,
        String providerCode,
        String providerRequestId,
        Boolean retryable,
        String errorCode,
        String errorMessage,
        Integer totalPdfCount,
        List<Map<String, Object>> invoiceItems,
        CoreOcrVatVerificationResponse vatVerification,
        Map<String, Object> rawResult
) {
}
