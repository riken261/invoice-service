package cloud.techotakus.invoice.core.api.model;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record InvoiceConfirmRequestModel(
        @NotBlank String nonce,
        Map<String, Object> confirmedFields
) {
}
