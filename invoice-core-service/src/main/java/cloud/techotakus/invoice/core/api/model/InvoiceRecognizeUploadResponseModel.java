package cloud.techotakus.invoice.core.api.model;

import java.time.OffsetDateTime;
import java.util.List;

public record InvoiceRecognizeUploadResponseModel(
        String batchId,
        String sessionId,
        String status,
        OffsetDateTime timeoutAt,
        Long nextPollAfterMs,
        List<InvoiceRecognizeSessionSummaryModel> sessions
) {
}
