package cloud.techotakus.invoice.file.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FileUploadRequestModel(
        @NotBlank String tenantId,
        @NotBlank String ownerUserId,
        @NotBlank String fileName,
        @NotBlank String mimeType,
        @NotNull @Positive Long size,
        @NotBlank String sha256,
        @NotBlank String objectKey,
        String contentBase64,
        String uploadNonce
) {
}
