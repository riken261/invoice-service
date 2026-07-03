package cloud.techotakus.invoice.authorization.domain.repository;

import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationLoginStateEntity;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationOidcUserEntity;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationTokenEntity;

public interface AuthKeycloakRepository {

    AuthorizationTokenEntity exchangeAuthorizationCode(AuthorizationLoginStateEntity loginState, String code);

    AuthorizationOidcUserEntity verifyAndReadClaims(AuthorizationLoginStateEntity loginState, AuthorizationTokenEntity token);

    void logout(String realm, String refreshToken);
}
