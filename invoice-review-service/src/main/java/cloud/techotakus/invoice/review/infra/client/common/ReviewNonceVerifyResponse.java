package cloud.techotakus.invoice.review.infra.client.common;

import java.time.OffsetDateTime;

public record ReviewNonceVerifyResponse(
        boolean valid,
        String operation,
        OffsetDateTime expiresAt
) {
}
