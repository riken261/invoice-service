package cloud.techotakus.invoice.claim.domain.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ClaimReviewRecordEntity {
    private String id;
    private String reviewerUserId;
    private String action;
    private String opinion;
    private String beforeStatus;
    private String afterStatus;
    private OffsetDateTime createdAt;
}
