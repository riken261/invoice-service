package cloud.techotakus.invoice.claim.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ClaimItemEntity {
    private String id;
    private String tenantId;
    private String claimId;
    private String invoiceId;
    private String expenseCategory;
    private BigDecimal amount;
    private String description;
    private OffsetDateTime createdAt;
    private String createdBy;
    private String createdTrace;
    private OffsetDateTime updatedAt;
    private String updatedBy;
    private String updatedTrace;
    private Boolean deleted;
}
