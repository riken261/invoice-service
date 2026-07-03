package cloud.techotakus.invoice.authorization.domain.repository;

import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationBffSessionEntity;
import java.util.Optional;

public interface AuthHttpRepository {

    Optional<String> sessionId();

    String loginRedirectUri(String redirectUri);

    void setSessionCookie(AuthorizationBffSessionEntity session);

    void expireSessionCookie();
}
