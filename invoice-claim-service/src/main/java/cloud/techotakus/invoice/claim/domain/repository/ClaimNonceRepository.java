package cloud.techotakus.invoice.claim.domain.repository;

public interface ClaimNonceRepository {

    void verifyAndConsume(String tenantId, String ownerUserId, String sessionId, String operation, String nonce);

    void verifyAndConsume(String tenantId, String ownerUserId, String sessionId, String operation, String resourceId, String nonce);
}
