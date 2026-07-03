package cloud.techotakus.invoice.authorization.domain.entity;

import java.util.List;

public record AuthorizationPermissionSetEntity(
    List<String> roles,
    List<String> permissions
) {
}
