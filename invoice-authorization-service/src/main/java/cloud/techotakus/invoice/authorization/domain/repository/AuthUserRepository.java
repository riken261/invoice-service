package cloud.techotakus.invoice.authorization.domain.repository;

import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationPermissionSetEntity;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationUserEntity;
import java.util.Optional;

public interface AuthUserRepository {

    Optional<String> findActiveTenantIdByCode(String tenantCode);

    Optional<AuthorizationUserEntity> findEnabledUser(String tenantId, String keycloakSubject, String email);

    AuthorizationPermissionSetEntity permissions(String tenantId, String userId);
}
