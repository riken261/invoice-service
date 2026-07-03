package cloud.techotakus.invoice.common.api.model;

import java.time.OffsetDateTime;

public record CommonNonceCreateResponseModel(
        String nonce,
        String operation,
        OffsetDateTime expiresAt
) {
}
