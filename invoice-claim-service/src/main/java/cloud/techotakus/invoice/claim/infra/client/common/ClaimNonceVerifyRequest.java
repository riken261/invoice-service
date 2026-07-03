package cloud.techotakus.invoice.claim.infra.client.common;

public record ClaimNonceVerifyRequest(
        String tenantId,
        String ownerUserId,
        String sessionId,
        String operation,
        String resourceType,
        String resourceId,
        String nonce
) {
}
