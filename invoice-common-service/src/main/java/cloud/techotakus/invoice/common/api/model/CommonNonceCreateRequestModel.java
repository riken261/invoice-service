package cloud.techotakus.invoice.common.api.model;

import jakarta.validation.constraints.NotBlank;

public record CommonNonceCreateRequestModel(
        @NotBlank String operation,
        String resourceType,
        String resourceId
) {
}
