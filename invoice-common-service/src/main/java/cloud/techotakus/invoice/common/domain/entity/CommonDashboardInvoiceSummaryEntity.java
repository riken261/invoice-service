package cloud.techotakus.invoice.common.domain.entity;

public record CommonDashboardInvoiceSummaryEntity(
        long ocrProcessingCount,
        long ocrConfirmRequiredCount,
        long manualInputRequiredCount,
        long rejectedInvoiceCount
) {
}
