package cloud.techotakus.invoice.common.infra.client.dashboard.model;

public record CommonDashboardFinanceSummaryResponse(
        long pendingInvoiceCount,
        long duplicateInvoiceCount,
        long pendingClaimCount,
        String averageWaitingHours
) {
}
