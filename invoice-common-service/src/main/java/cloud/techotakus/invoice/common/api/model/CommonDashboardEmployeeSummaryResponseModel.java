package cloud.techotakus.invoice.common.api.model;

public record CommonDashboardEmployeeSummaryResponseModel(
        long ocrProcessingCount,
        long ocrConfirmRequiredCount,
        long manualInputRequiredCount,
        long rejectedInvoiceCount,
        long draftClaimCount,
        long rejectedClaimCount
) {
}
