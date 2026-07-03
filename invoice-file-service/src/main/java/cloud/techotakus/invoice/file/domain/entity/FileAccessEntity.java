package cloud.techotakus.invoice.file.domain.entity;

import java.time.OffsetDateTime;

public record FileAccessEntity(
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
