package cloud.techotakus.invoice.ocr.api.model;

import java.util.List;
import java.util.Map;

public record OcrRecognizeResponseModel(
        boolean success,
        String providerCode,
        String providerRequestId,
        Boolean retryable,
        String errorCode,
        String errorMessage,
        Integer totalPdfCount,
        List<Map<String, Object>> invoiceItems,
        OcrVatVerificationResponseModel vatVerification,
        Map<String, Object> rawResult
) {
}
