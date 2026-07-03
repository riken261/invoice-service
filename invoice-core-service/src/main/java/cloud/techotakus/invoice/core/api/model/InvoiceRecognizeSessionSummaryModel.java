package cloud.techotakus.invoice.core.api.model;

import java.time.OffsetDateTime;

public record InvoiceRecognizeSessionSummaryModel(
        String sessionId,
        String fileName,
        String status,
        OffsetDateTime timeoutAt
) {
}
