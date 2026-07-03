package cloud.techotakus.invoice.authorization.domain.repository;

import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationBffSessionEntity;
import java.util.Optional;

public interface AuthBffSessionRepository {

    void save(AuthorizationBffSessionEntity entity);

    Optional<AuthorizationBffSessionEntity> find(String sessionId);

    void remove(String sessionId);
}
