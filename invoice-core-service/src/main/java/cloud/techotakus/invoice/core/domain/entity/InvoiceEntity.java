package cloud.techotakus.invoice.core.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
public class InvoiceEntity {
    private String id;
    private String tenantId;
    private String invoiceFileId;
    private String ownerUserId;
    private String invoiceType;
    private String invoiceCode;
    private String invoiceNumber;
    private LocalDate issueDate;
    private String sellerName;
    private String buyerName;
    private BigDecimal amountWithoutTax;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String currency;
    private String ocrStatus;
    private String invoiceStatus;
    private String duplicateStatus;
    private Boolean manualInput;
    private Map<String, Object> ocrFields;
    private Map<String, Object> confirmedFields;
    private Map<String, Object> manualFields;
    private String latestReviewOpinion;
    private OffsetDateTime createdAt;
    private String createdBy;
    private String createdTrace;
    private OffsetDateTime updatedAt;
    private String updatedBy;
    private String updatedTrace;
    private Boolean deleted;
}
