package cloud.techotakus.invoice.review.domain.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ReviewClaimDecisionEntity {
    private String tenantId;
    private String reviewerUserId;
    private String sessionId;
    private String claimId;
    private String nonce;
    private String idempotencyKey;
    private String comment;
    private String status;
    private String reviewId;
    private String action;
    private OffsetDateTime reviewedAt;
}
