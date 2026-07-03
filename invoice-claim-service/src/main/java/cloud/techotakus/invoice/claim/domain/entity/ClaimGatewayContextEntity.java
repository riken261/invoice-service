package cloud.techotakus.invoice.claim.domain.entity;

public record ClaimGatewayContextEntity(
        String sessionId,
        String tenantId,
        String userId
) {
}
