package cloud.techotakus.invoice.review.domain.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ReviewClaimReviewRecordEntity {
    private String id;
    private String tenantId;
    private String resourceType;
    private String resourceId;
    private String reviewerUserId;
    private String action;
    private String opinion;
    private String beforeStatus;
    private String afterStatus;
    private OffsetDateTime createdAt;
    private String createdBy;
    private String createdTrace;
    private OffsetDateTime updatedAt;
    private String updatedBy;
    private String updatedTrace;
    private Boolean deleted;
}
