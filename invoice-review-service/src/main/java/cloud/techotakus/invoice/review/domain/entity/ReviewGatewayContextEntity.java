package cloud.techotakus.invoice.review.domain.entity;

public record ReviewGatewayContextEntity(
        String sessionId,
        String tenantId,
        String userId
) {
}
