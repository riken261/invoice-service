package cloud.techotakus.invoice.file.api.model;

import java.time.OffsetDateTime;

public record FileAccessResponseModel(
        String fileId,
        String url,
        String method,
        Integer expiresInSeconds,
        OffsetDateTime expiresAt,
        String filename,
        String contentType,
        String accessType
) {
}
