package cloud.techotakus.invoice.claim.infra.client.common;

import java.time.OffsetDateTime;

public record ClaimNonceVerifyResponse(
        boolean valid,
        String operation,
        OffsetDateTime expiresAt
) {
}
