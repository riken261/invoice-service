package cloud.techotakus.invoice.core.api.model;

public record InvoiceDashboardEmployeeSummaryModel(
        long ocrProcessingCount,
        long ocrConfirmRequiredCount,
        long manualInputRequiredCount,
        long rejectedInvoiceCount
) {
}
