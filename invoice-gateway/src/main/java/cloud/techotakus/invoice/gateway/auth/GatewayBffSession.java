package cloud.techotakus.invoice.gateway.auth;

import java.time.Instant;
import java.util.List;

public record GatewayBffSession(
    String sessionId,
    String tenantId,
    String tenantCode,
    String keycloakRealm,
    String userId,
    String username,
    String displayName,
    String email,
    List<String> roles,
    List<String> permissions,
    String accessToken,
    String refreshToken,
    Instant expiresAt
) {
    public boolean expired(Instant now) {
        return !expiresAt.isAfter(now);
    }
}
