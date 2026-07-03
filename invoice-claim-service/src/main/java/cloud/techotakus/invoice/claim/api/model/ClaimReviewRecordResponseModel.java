package cloud.techotakus.invoice.claim.api.model;

import java.time.OffsetDateTime;

public record ClaimReviewRecordResponseModel(
        String reviewId,
        String action,
        String actor,
        String reviewerId,
        String comment,
        String beforeStatus,
        String afterStatus,
        OffsetDateTime occurredAt,
        OffsetDateTime createdAt
) {
}
