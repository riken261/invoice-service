package cloud.techotakus.invoice.core.api.model;

import java.util.Map;

public record InvoiceConfirmResponseModel(
        InvoiceSummaryModel summary,
        Map<String, Object> confirmedFields,
        Map<String, Object> ocrRawResultSummary
) {
}
