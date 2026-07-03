package cloud.techotakus.invoice.core.api.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InvoiceRecognizeUploadRequestModel(
        @NotBlank String nonce,
        @Valid @NotEmpty List<InvoiceRecognizeUploadFileModel> files
) {
}
