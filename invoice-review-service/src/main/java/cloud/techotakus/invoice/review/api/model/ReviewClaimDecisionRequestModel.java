package cloud.techotakus.invoice.review.api.model;

import jakarta.validation.constraints.NotBlank;

public record ReviewClaimDecisionRequestModel(
        @NotBlank String nonce,
        String idempotencyKey,
        String comment
) {
}
