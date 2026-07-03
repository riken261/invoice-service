package cloud.techotakus.invoice.review.api.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ReviewClaimDetailResponseModel(
        String id,
        String claimId,
        String claimNo,
        String applicant,
        String applicantUserId,
        String title,
        String description,
        String costCenter,
        BigDecimal totalAmount,
        BigDecimal amount,
        String currency,
        String status,
        String expenseCategory,
        List<String> invoiceIds,
        List<ReviewClaimInvoiceResponseModel> invoices,
        OffsetDateTime submittedAt,
        OffsetDateTime createdAt
) {
}
