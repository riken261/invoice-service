package cloud.techotakus.invoice.core.api.model;

import java.util.List;

public record InvoiceRecognizeBatchResultResponseModel(
        String batchId,
        String status,
        boolean completed,
        Long nextPollAfterMs,
        List<InvoiceRecognizeResultResponseModel> sessions
) {
}
