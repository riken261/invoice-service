package cloud.techotakus.invoice.common.domain.entity;

public record CommonGatewayContextEntity(
    String sessionId,
    String tenantId,
    String userId
) {
}
