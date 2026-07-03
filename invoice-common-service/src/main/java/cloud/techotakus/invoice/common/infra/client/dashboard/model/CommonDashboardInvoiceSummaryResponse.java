package cloud.techotakus.invoice.common.infra.client.dashboard.model;

public record CommonDashboardInvoiceSummaryResponse(
        long ocrProcessingCount,
        long ocrConfirmRequiredCount,
        long manualInputRequiredCount,
        long rejectedInvoiceCount
) {
}
