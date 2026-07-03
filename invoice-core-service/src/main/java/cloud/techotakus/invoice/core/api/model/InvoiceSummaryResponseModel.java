package cloud.techotakus.invoice.core.api.model;

import java.math.BigDecimal;

public record InvoiceSummaryResponseModel(
        String invoiceId,
        String invoiceNo,
        String invoiceType,
        String invoiceDate,
        BigDecimal totalAmount,
        String status
) {
}
