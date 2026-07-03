package cloud.techotakus.invoice.claim.api.model;

public record ClaimDashboardEmployeeSummaryModel(
        long draftClaimCount,
        long rejectedClaimCount
) {
}
