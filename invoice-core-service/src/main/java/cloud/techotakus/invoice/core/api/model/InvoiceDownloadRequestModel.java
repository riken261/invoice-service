package cloud.techotakus.invoice.core.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InvoiceDownloadRequestModel(
        @NotEmpty List<@NotBlank String> invoiceIds
) {
}
