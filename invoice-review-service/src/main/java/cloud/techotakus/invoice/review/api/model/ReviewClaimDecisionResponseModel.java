package cloud.techotakus.invoice.review.api.model;

import java.time.OffsetDateTime;

public record ReviewClaimDecisionResponseModel(
        String claimId,
        String status,
        String reviewId,
        String action,
        String comment,
        OffsetDateTime reviewedAt
) {
}
