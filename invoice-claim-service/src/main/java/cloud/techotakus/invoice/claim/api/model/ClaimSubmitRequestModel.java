package cloud.techotakus.invoice.claim.api.model;

import jakarta.validation.constraints.NotBlank;

public record ClaimSubmitRequestModel(
        @NotBlank String nonce,
        String idempotencyKey
) {
}
