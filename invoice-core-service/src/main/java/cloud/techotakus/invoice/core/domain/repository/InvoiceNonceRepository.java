package cloud.techotakus.invoice.core.domain.repository;

public interface InvoiceNonceRepository {

    void verifyAndConsume(String tenantId, String ownerUserId, String sessionId, String operation, String nonce);

    void verifyAndConsume(String tenantId, String ownerUserId, String sessionId, String operation, String resourceId, String nonce);
}
