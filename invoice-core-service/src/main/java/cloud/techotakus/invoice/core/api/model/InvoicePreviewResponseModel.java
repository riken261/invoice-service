package cloud.techotakus.invoice.core.api.model;

import java.time.OffsetDateTime;

public record InvoicePreviewResponseModel(
        String invoiceId,
        String fileId,
        String imageUrl,
        String method,
        Integer expiresInSeconds,
        OffsetDateTime expiresAt,
        String filename,
        String contentType
) {
}
