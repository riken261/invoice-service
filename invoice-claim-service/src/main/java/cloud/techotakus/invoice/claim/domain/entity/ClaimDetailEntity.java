package cloud.techotakus.invoice.claim.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ClaimDetailEntity {
    private String id;
    private String claimNo;
    private String applicantUserId;
    private String description;
    private BigDecimal totalAmount;
    private String currency;
    private String status;
    private String expenseCategory;
    private List<String> invoiceIds = new ArrayList<>();
    private List<ClaimRecordEntity> invoices = new ArrayList<>();
    private OffsetDateTime submittedAt;
    private OffsetDateTime createdAt;
}
