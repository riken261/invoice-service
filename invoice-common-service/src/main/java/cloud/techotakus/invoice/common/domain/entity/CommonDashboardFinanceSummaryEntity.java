package cloud.techotakus.invoice.common.domain.entity;

public record CommonDashboardFinanceSummaryEntity(
        long pendingInvoiceCount,
        long duplicateInvoiceCount,
        long pendingClaimCount,
        String averageWaitingHours
) {
}
