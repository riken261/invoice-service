package cloud.techotakus.invoice.authorization.domain.entity;

import java.time.Instant;

public record AuthorizationLoginStateEntity(
    String state,
    String loginUrl,
    String tenantCode,
    String keycloakRealm,
    String redirectUri,
    Instant expiresAt
) {
    public boolean expired(Instant now) {
        return !expiresAt.isAfter(now);
    }
}
