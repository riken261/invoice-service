package cloud.techotakus.invoice.review.api.model;

public record ReviewDashboardFinanceSummaryModel(
        long pendingInvoiceCount,
        long duplicateInvoiceCount,
        long pendingClaimCount,
        String averageWaitingHours
) {
}
