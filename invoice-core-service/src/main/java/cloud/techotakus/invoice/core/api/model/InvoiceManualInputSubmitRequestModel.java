package cloud.techotakus.invoice.core.api.model;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record InvoiceManualInputSubmitRequestModel(
        @NotBlank String nonce,
        String idempotencyKey,
        Map<String, Object> fields
) {
}
