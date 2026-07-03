package cloud.techotakus.invoice.claim.api.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ClaimSubmitResponseModel(
        String claimId,
        String id,
        String claimNo,
        String status,
        BigDecimal totalAmount,
        String currency,
        List<String> invoiceIds,
        OffsetDateTime submittedAt
) {
}
