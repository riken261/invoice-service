package cloud.techotakus.invoice.common.infra.client.dashboard.model;

public record CommonDashboardClaimSummaryResponse(
        long draftClaimCount,
        long rejectedClaimCount
) {
}
