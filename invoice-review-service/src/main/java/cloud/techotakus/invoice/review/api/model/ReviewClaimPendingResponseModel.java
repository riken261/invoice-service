package cloud.techotakus.invoice.review.api.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReviewClaimPendingResponseModel(
        String targetId,
        String claimId,
        String id,
        String claimNo,
        String title,
        String applicant,
        String applicantUserId,
        BigDecimal amount,
        BigDecimal totalAmount,
        String currency,
        String status,
        Integer invoiceCount,
        OffsetDateTime submittedAt,
        OffsetDateTime createdAt
) {
}
