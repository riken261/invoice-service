package cloud.techotakus.invoice.core.infra.client.common;

public record CoreNonceVerifyRequest(
        String tenantId,
        String ownerUserId,
        String sessionId,
        String operation,
        String resourceType,
        String resourceId,
        String nonce
) {
}
