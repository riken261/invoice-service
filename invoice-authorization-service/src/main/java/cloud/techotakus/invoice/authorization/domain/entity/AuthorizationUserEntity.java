package cloud.techotakus.invoice.authorization.domain.entity;

public record AuthorizationUserEntity(
    String userId,
    String tenantId,
    String keycloakSubject,
    String username,
    String displayName,
    String email,
    String departmentId
) {
}
