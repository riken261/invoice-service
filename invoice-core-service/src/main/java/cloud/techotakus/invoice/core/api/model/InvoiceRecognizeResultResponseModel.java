package cloud.techotakus.invoice.core.api.model;

import java.time.OffsetDateTime;
import java.util.Map;

public record InvoiceRecognizeResultResponseModel(
        String sessionId,
        String tenantId,
        String ownerUserId,
        String fileId,
        String status,
        String provider,
        String rawRequestId,
        Map<String, Object> rawResult,
        Map<String, Object> normalizedResult,
        String errorCode,
        String errorMessage,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
