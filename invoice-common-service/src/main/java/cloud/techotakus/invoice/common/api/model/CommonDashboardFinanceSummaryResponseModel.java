package cloud.techotakus.invoice.common.api.model;

public record CommonDashboardFinanceSummaryResponseModel(
        long pendingInvoiceCount,
        long duplicateInvoiceCount,
        long pendingClaimCount,
        String averageWaitingHours
) {
}
