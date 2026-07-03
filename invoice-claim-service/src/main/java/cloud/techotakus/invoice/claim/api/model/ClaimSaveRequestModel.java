package cloud.techotakus.invoice.claim.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ClaimSaveRequestModel(
        @NotBlank String nonce,
        String idempotencyKey,
        String title,
        String costCenter,
        String description,
        @NotBlank String expenseCategory,
        @NotEmpty List<String> invoiceIds
) {
}
