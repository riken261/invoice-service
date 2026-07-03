package cloud.techotakus.invoice.review.infra.client.common;

public record ReviewNonceVerifyRequest(
        String tenantId,
        String ownerUserId,
        String sessionId,
        String operation,
        String resourceType,
        String resourceId,
        String nonce
) {
}
