package cloud.techotakus.invoice.authorization.domain.entity;

import java.util.List;

public record AuthorizationOidcUserEntity(
    String subject,
    String tenantId,
    String tenantCode,
    String username,
    String displayName,
    String email,
    List<String> roles,
    List<String> permissions
) {
}
