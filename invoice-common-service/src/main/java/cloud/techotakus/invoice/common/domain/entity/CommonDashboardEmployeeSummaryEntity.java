package cloud.techotakus.invoice.common.domain.entity;

public record CommonDashboardEmployeeSummaryEntity(
        long ocrProcessingCount,
        long ocrConfirmRequiredCount,
        long manualInputRequiredCount,
        long rejectedInvoiceCount,
        long draftClaimCount,
        long rejectedClaimCount
) {
}
