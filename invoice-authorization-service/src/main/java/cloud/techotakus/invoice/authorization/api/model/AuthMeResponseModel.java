package cloud.techotakus.invoice.authorization.api.model;

import java.util.List;

public record AuthMeResponseModel(
    String sessionId,
    String tenantId,
    String tenantCode,
    String userId,
    String username,
    String displayName,
    String email,
    List<String> roles,
    List<String> permissions
) {
}
