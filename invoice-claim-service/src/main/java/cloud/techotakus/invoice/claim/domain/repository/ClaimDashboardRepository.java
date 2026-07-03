package cloud.techotakus.invoice.claim.domain.repository;

public interface ClaimDashboardRepository {

    long countByStatus(String tenantId, String userId, String status);
}
