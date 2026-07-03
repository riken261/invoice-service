package cloud.techotakus.invoice.authorization.infra.dao;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.authorization.config.AuthProperties;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationLoginStateEntity;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationOidcUserEntity;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationTokenEntity;
import cloud.techotakus.invoice.authorization.domain.repository.AuthKeycloakRepository;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Repository
public class AuthKeycloakDao implements AuthKeycloakRepository {

    private final AuthProperties properties;
    private final RestClient restClient = RestClient.create();

    public AuthKeycloakDao(AuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public AuthorizationTokenEntity exchangeAuthorizationCode(AuthorizationLoginStateEntity loginState, String code) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.getKeycloak().getClientId());
        form.add("code", code);
        form.add("redirect_uri", loginState.redirectUri());
        if (StringUtils.hasText(properties.getKeycloak().getClientSecret())) {
            form.add("client_secret", properties.getKeycloak().getClientSecret());
        }

        Map<?, ?> body = restClient.post()
            .uri(tokenEndpoint(loginState.keycloakRealm()))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(Map.class);
        if (body == null) {
            throw new ServiceException("empty keycloak token response", ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE);
        }
        return new AuthorizationTokenEntity(
            stringValue(body.get("access_token")),
            stringValue(body.get("refresh_token")),
            stringValue(body.get("id_token")),
            longValue(body.get("expires_in"))
        );
    }

    @Override
    public AuthorizationOidcUserEntity verifyAndReadClaims(
        AuthorizationLoginStateEntity loginState,
        AuthorizationTokenEntity token
    ) {
        Jwt jwt = NimbusJwtDecoder.withIssuerLocation(issuer(loginState.keycloakRealm()))
            .build()
            .decode(token.idToken());
        Set<String> roleClaims = roleClaims(jwt.getClaims());
        return new AuthorizationOidcUserEntity(
            jwt.getSubject(),
            firstPresent(jwt.getClaimAsString("tenant_id"), loginState.tenantCode()),
            loginState.tenantCode(),
            firstPresent(jwt.getClaimAsString("preferred_username"), jwt.getClaimAsString("email")),
            firstPresent(jwt.getClaimAsString("name"), jwt.getClaimAsString("preferred_username")),
            jwt.getClaimAsString("email"),
            roleClaims.stream().sorted().toList(),
            List.of()
        );
    }

    @Override
    public void logout(String realm, String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return;
        }
        var form = new LinkedMultiValueMap<String, String>();
        form.add("client_id", properties.getKeycloak().getClientId());
        form.add("refresh_token", refreshToken);
        if (StringUtils.hasText(properties.getKeycloak().getClientSecret())) {
            form.add("client_secret", properties.getKeycloak().getClientSecret());
        }

        restClient.post()
            .uri(logoutEndpoint(realm))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .toBodilessEntity();
    }

    private String tokenEndpoint(String realm) {
        return issuer(realm) + "/protocol/openid-connect/token";
    }

    private String logoutEndpoint(String realm) {
        return issuer(realm) + "/protocol/openid-connect/logout";
    }

    private String issuer(String realm) {
        return properties.getKeycloak().getIssuerBaseUri().replaceAll("/+$", "") + "/" + realm;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0;
        }
        return Long.parseLong(String.valueOf(value));
    }

    private String firstPresent(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }

    private Set<String> roleClaims(Map<String, Object> claims) {
        var roles = new LinkedHashSet<String>();
        addRoles(roles, claims.get("roles"));
        addRoles(roles, claims.get("groups"));
        addRoles(roles, nestedRoles(claims.get("realm_access")));
        addRoles(roles, clientRoles(claims.get("resource_access")));
        return roles;
    }

    private void addRoles(Set<String> target, Object source) {
        target.addAll(stringSet(source));
    }

    private Set<String> stringSet(Object value) {
        if (value instanceof Collection<?> values) {
            var result = new LinkedHashSet<String>();
            for (Object item : values) {
                if (item != null && StringUtils.hasText(String.valueOf(item))) {
                    result.add(String.valueOf(item));
                }
            }
            return result;
        }
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return Set.of();
        }
        return Set.of(String.valueOf(value));
    }

    private Object nestedRoles(Object value) {
        if (value instanceof Map<?, ?> map) {
            return map.get("roles");
        }
        return null;
    }

    private List<String> clientRoles(Object value) {
        var roles = new LinkedHashSet<String>();
        if (!(value instanceof Map<?, ?> resourceAccess)) {
            return List.of();
        }
        addRoles(roles, nestedRoles(resourceAccess.get(properties.getKeycloak().getClientId())));
        for (Object clientAccess : resourceAccess.values()) {
            addRoles(roles, nestedRoles(clientAccess));
        }
        return List.copyOf(roles);
    }
}
