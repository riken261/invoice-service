package cloud.techotakus.invoice.core.domain.repository;

public interface InvoiceDashboardRepository {

    long countByOcrStatus(String tenantId, String userId, String status);

    long countByInvoiceStatus(String tenantId, String userId, String... statuses);
}
