package cloud.techotakus.invoice.review.domain.repository;

public interface ReviewNonceRepository {

    void verifyAndConsume(String tenantId, String ownerUserId, String sessionId, String operation, String resourceType, String resourceId, String nonce);
}
