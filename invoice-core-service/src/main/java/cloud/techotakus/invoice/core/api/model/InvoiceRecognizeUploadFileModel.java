package cloud.techotakus.invoice.core.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InvoiceRecognizeUploadFileModel(
        @NotBlank String fileName,
        @NotBlank String mimeType,
        @NotNull @Positive Long size,
        @NotBlank String sha256,
        @NotBlank String objectKey,
        @NotBlank String contentBase64,
        String invoiceTypeHint
) {
}
