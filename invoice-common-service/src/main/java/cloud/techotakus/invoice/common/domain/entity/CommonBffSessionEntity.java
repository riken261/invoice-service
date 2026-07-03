package cloud.techotakus.invoice.common.domain.entity;

import java.time.Instant;
import java.util.Set;

public record CommonBffSessionEntity(
        String sessionId,
        String tenantId,
        String tenantCode,
        String keycloakRealm,
        String userId,
        String username,
        String displayName,
        String email,
        String departmentId,
        Set<String> roles,
        Set<String> permissions,
        String accessToken,
        String refreshToken,
        Instant expiresAt
) {
    public boolean expired(Instant now) {
        return !expiresAt.isAfter(now);
    }
}
