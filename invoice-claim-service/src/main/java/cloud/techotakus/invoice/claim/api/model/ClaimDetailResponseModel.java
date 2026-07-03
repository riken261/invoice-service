package cloud.techotakus.invoice.claim.api.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ClaimDetailResponseModel(
        String id,
        String claimId,
        String claimNo,
        String applicantUserId,
        String title,
        String description,
        String costCenter,
        BigDecimal totalAmount,
        String currency,
        String status,
        String expenseCategory,
        List<String> invoiceIds,
        List<ClaimRecordResponseModel> invoices,
        OffsetDateTime submittedAt,
        OffsetDateTime createdAt
) {
}
