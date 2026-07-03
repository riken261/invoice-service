package cloud.techotakus.invoice.review.api.model;

import java.time.OffsetDateTime;

public record ReviewClaimReviewRecordResponseModel(
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
