package cloud.techotakus.invoice.common.api.model;

import java.time.OffsetDateTime;

public record CommonNonceVerifyResponseModel(
        boolean valid,
        String operation,
        OffsetDateTime expiresAt
) {
}
