package cloud.techotakus.invoice.review.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ReviewClaimPendingEntity {
    private String id;
    private String claimNo;
    private String applicantUserId;
    private String description;
    private BigDecimal totalAmount;
    private String currency;
    private String status;
    private Integer invoiceCount;
    private OffsetDateTime submittedAt;
    private OffsetDateTime createdAt;
}
