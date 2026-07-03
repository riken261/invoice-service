package cloud.techotakus.invoice.core.api.model;

import java.util.List;

public record InvoiceDownloadResponseModel(
        List<InvoiceDownloadItemModel> files
) {
}
