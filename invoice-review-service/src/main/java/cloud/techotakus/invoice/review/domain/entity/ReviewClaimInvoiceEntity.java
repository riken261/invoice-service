package cloud.techotakus.invoice.review.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ReviewClaimInvoiceEntity {
    private String id;
    private String invoiceNumber;
    private String sellerName;
    private BigDecimal totalAmount;
    private String currency;
    private String expenseCategory;
    private String fileName;
    private String invoiceStatus;
    private OffsetDateTime createdAt;
}
