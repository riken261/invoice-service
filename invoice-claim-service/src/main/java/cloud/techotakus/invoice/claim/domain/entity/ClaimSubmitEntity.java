package cloud.techotakus.invoice.claim.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ClaimSubmitEntity {
    private String id;
    private String tenantId;
    private String applicantUserId;
    private String sessionId;
    private String nonce;
    private String idempotencyKey;
    private String title;
    private String description;
    private String expenseCategory;
    private List<String> invoiceIds = new ArrayList<>();
    private String claimNo;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private OffsetDateTime submittedAt;
    private OffsetDateTime createdAt;
    private String createdBy;
    private String createdTrace;
    private OffsetDateTime updatedAt;
    private String updatedBy;
    private String updatedTrace;
    private Boolean deleted;
}
