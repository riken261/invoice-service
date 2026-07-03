package cloud.techotakus.invoice.common.domain.entity;

public record CommonDashboardClaimSummaryEntity(
        long draftClaimCount,
        long rejectedClaimCount
) {
}
