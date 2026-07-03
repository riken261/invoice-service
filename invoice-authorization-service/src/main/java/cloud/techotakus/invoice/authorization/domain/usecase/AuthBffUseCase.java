package cloud.techotakus.invoice.authorization.domain.usecase;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.authorization.config.AuthProperties;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationBffSessionEntity;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationCallbackEntity;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationLoginStateEntity;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationOidcUserEntity;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationPermissionSetEntity;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationTokenEntity;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationUserEntity;
import cloud.techotakus.invoice.authorization.domain.repository.AuthBffSessionRepository;
import cloud.techotakus.invoice.authorization.domain.repository.AuthHttpRepository;
import cloud.techotakus.invoice.authorization.domain.repository.AuthKeycloakRepository;
import cloud.techotakus.invoice.authorization.domain.repository.AuthLoginStateRepository;
import cloud.techotakus.invoice.authorization.domain.repository.AuthUserRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthBffUseCase {

    private final AuthProperties properties;
    private final AuthLoginStateRepository loginStateRepository;
    private final AuthBffSessionRepository sessionRepository;
    private final AuthKeycloakRepository keycloakRepository;
    private final AuthHttpRepository httpRepository;
    private final AuthUserRepository userRepository;
    private final Clock clock = Clock.systemUTC();

    public AuthBffUseCase(
        AuthProperties properties,
        AuthLoginStateRepository loginStateRepository,
        AuthBffSessionRepository sessionRepository,
        AuthKeycloakRepository keycloakRepository,
        AuthHttpRepository httpRepository,
        AuthUserRepository userRepository
    ) {
        this.properties = properties;
        this.loginStateRepository = loginStateRepository;
        this.sessionRepository = sessionRepository;
        this.keycloakRepository = keycloakRepository;
        this.httpRepository = httpRepository;
        this.userRepository = userRepository;
    }

    public AuthorizationLoginStateEntity loginUrl(String tenantCode, String redirectUri) {
        required(tenantCode, "tenantCode");
        String realm = properties.getKeycloak().getRealm();
        String state = UUID.randomUUID().toString();
        String callbackUri = httpRepository.loginRedirectUri(redirectUri);
        Instant expiresAt = Instant.now(clock).plus(properties.getLoginState().getExpiresIn());
        AuthorizationLoginStateEntity entity = new AuthorizationLoginStateEntity(
            state,
            authorizationUrl(realm, state, callbackUri),
            tenantCode.trim(),
            realm,
            callbackUri,
            expiresAt
        );
        loginStateRepository.save(entity);
        return entity;
    }

    public AuthorizationCallbackEntity callback(String state, String code) {
        required(state, "state");
        required(code, "code");
        AuthorizationLoginStateEntity loginState = loginStateRepository.consume(state)
            .orElseThrow(() -> new ServiceException("invalid login state", ErrorCode.UNAUTHORIZED));
        AuthorizationTokenEntity token = keycloakRepository.exchangeAuthorizationCode(loginState, code);
        AuthorizationOidcUserEntity oidcUser = keycloakRepository.verifyAndReadClaims(loginState, token);
        String tenantId = userRepository.findActiveTenantIdByCode(loginState.tenantCode())
            .orElseThrow(() -> new ServiceException("tenant not found", ErrorCode.TENANT_NOT_FOUND));
        AuthorizationUserEntity user = userRepository
            .findEnabledUser(tenantId, oidcUser.subject(), oidcUser.email())
            .orElseThrow(() -> new ServiceException("SSO user is not provisioned", ErrorCode.UNAUTHORIZED));
        AuthorizationPermissionSetEntity permissionSet = userRepository.permissions(user.tenantId(), user.userId());
        AuthorizationBffSessionEntity session = buildSession(loginState, user, permissionSet, token);
        sessionRepository.save(session);
        httpRepository.setSessionCookie(session);
        return new AuthorizationCallbackEntity(properties.getKeycloak().getRedirectUrl(), session);
    }

    public AuthorizationBffSessionEntity me() {
        String sessionId = httpRepository.sessionId()
            .orElseThrow(() -> new ServiceException("not logged in", ErrorCode.UNAUTHORIZED));
        return sessionRepository.find(sessionId)
            .orElseThrow(() -> new ServiceException("session expired", ErrorCode.UNAUTHORIZED));
    }

    public Boolean logout() {
        httpRepository.sessionId()
            .flatMap(sessionRepository::find)
            .ifPresent(session -> {
                keycloakRepository.logout(session.keycloakRealm(), session.refreshToken());
                sessionRepository.remove(session.sessionId());
            });
        httpRepository.expireSessionCookie();
        return true;
    }

    private AuthorizationBffSessionEntity buildSession(
        AuthorizationLoginStateEntity loginState,
        AuthorizationUserEntity user,
        AuthorizationPermissionSetEntity permissionSet,
        AuthorizationTokenEntity token
    ) {
        required(user.userId(), "userId");
        String username = StringUtils.hasText(user.username()) ? user.username() : user.email();
        String displayName = StringUtils.hasText(user.displayName()) ? user.displayName() : username;
        return new AuthorizationBffSessionEntity(
            UUID.randomUUID().toString(),
            user.tenantId(),
            loginState.tenantCode(),
            loginState.keycloakRealm(),
            user.userId(),
            username,
            displayName,
            user.email(),
            sorted(permissionSet.roles()),
            sorted(permissionSet.permissions()),
            token.accessToken(),
            token.refreshToken(),
            Instant.now(clock).plus(properties.getSession().getExpiresIn())
        );
    }

    private String authorizationUrl(String realm, String state, String redirectUri) {
        return issuer(realm) + "/protocol/openid-connect/auth"
            + "?response_type=code"
            + "&scope=" + encode("openid profile email")
            + "&client_id=" + encode(properties.getKeycloak().getClientId())
            + "&redirect_uri=" + encode(redirectUri)
            + "&state=" + encode(state);
    }

    private String issuer(String realm) {
        return properties.getKeycloak().getIssuerBaseUri().replaceAll("/+$", "") + "/" + realm;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private List<String> sorted(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().filter(StringUtils::hasText).distinct().sorted().toList();
    }

    private void required(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ServiceException(fieldName + " is required", ErrorCode.VALIDATION_ERROR);
        }
    }
}
