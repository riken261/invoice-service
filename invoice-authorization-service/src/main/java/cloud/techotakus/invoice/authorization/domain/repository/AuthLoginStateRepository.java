package cloud.techotakus.invoice.authorization.domain.repository;

import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationLoginStateEntity;
import java.util.Optional;

public interface AuthLoginStateRepository {

    void save(AuthorizationLoginStateEntity entity);

    Optional<AuthorizationLoginStateEntity> consume(String state);
}
