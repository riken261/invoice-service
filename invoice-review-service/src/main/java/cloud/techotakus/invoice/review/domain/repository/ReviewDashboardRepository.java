package cloud.techotakus.invoice.review.domain.repository;

public interface ReviewDashboardRepository {

    long countInvoicesByStatus(String tenantId, String... statuses);

    long countInvoicesByDuplicateStatus(String tenantId, String... statuses);

    long countClaimsByStatus(String tenantId, String... statuses);

    String averagePendingClaimWaitingHours(String tenantId);
}
