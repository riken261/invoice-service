package cloud.techotakus.invoice.common.api.model;

import jakarta.validation.constraints.NotBlank;

public record CommonNonceVerifyRequestModel(
        @NotBlank String tenantId,
        @NotBlank String ownerUserId,
        @NotBlank String sessionId,
        @NotBlank String operation,
        String resourceType,
        String resourceId,
        @NotBlank String nonce
) {
}
