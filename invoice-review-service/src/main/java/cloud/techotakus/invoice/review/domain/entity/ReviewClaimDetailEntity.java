package cloud.techotakus.invoice.review.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReviewClaimDetailEntity {
    private String id;
    private String claimNo;
    private String applicantUserId;
    private String description;
    private BigDecimal totalAmount;
    private String currency;
    private String status;
    private String latestReviewOpinion;
    private String expenseCategory;
    private List<String> invoiceIds = new ArrayList<>();
    private List<ReviewClaimInvoiceEntity> invoices = new ArrayList<>();
    private OffsetDateTime submittedAt;
    private OffsetDateTime approvedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String updatedBy;
    private String updatedTrace;
}
