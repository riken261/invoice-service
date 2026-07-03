package cloud.techotakus.invoice.core.domain.entity;

public record InvoiceGatewayContextEntity(
    String sessionId,
    String tenantId,
    String userId
) {
}
