package cloud.techotakus.invoice.claim.api.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ClaimRecordResponseModel(
        String id,
        String invoiceId,
        String invoiceNumber,
        String seller,
        String sellerName,
        BigDecimal amount,
        BigDecimal totalAmount,
        String currency,
        String expenseCategory,
        String fileName,
        String status,
        OffsetDateTime submittedAt
) {
}
