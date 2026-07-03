package cloud.techotakus.invoice.core.infra.client.common;

import java.time.OffsetDateTime;

public record CoreNonceVerifyResponse(
        boolean valid,
        String operation,
        OffsetDateTime expiresAt
) {
}
