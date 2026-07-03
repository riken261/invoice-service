package cloud.techotakus.invoice.core.api.model;

import java.time.OffsetDateTime;

public record InvoiceDownloadItemModel(
        String invoiceId,
        String fileId,
        String downloadUrl,
        String method,
        Integer expiresInSeconds,
        OffsetDateTime expiresAt,
        String filename,
        String contentType
) {
}
