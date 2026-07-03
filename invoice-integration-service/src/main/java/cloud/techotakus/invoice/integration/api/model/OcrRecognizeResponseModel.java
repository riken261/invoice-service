package cloud.techotakus.invoice.integration.api.model;

import cloud.techotakus.invoice.integration.domain.entity.OcrInvoiceItem;
import cloud.techotakus.invoice.integration.domain.entity.OcrVatVerificationResponseEntity;

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
        List<OcrInvoiceItem> invoiceItems,
        OcrVatVerificationResponseEntity vatVerification,
        Map<String, Object> rawResult
) {
}
