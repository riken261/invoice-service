package cloud.techotakus.invoice.core.api.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public record InvoiceDetailResponseModel(
        String invoiceId,
        String fileId,
        String invoiceNo,
        String invoiceType,
        String invoiceCode,
        String invoiceDate,
        String sellerName,
        String buyerName,
        BigDecimal amountWithoutTax,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String currency,
        String ocrStatus,
        String status,
        String duplicateStatus,
        Boolean manualInput,
        Map<String, Object> ocrFields,
        Map<String, Object> confirmedFields,
        Map<String, Object> manualFields,
        String latestReviewOpinion,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
