package cloud.techotakus.invoice.core.infra.client.file;

import java.time.OffsetDateTime;

public record CoreFileAccessResponse(
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
