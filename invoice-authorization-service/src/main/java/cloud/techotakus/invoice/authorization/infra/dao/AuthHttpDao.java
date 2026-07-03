package cloud.techotakus.invoice.authorization.infra.dao;

import cloud.techotakus.invoice.authorization.config.AuthProperties;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationBffSessionEntity;
import cloud.techotakus.invoice.authorization.domain.repository.AuthHttpRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Repository
public class AuthHttpDao implements AuthHttpRepository {

    private final AuthProperties properties;

    public AuthHttpDao(AuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<String> sessionId() {
        Cookie[] cookies = request().getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
            .filter(cookie -> properties.getSession().getCookieName().equals(cookie.getName()))
            .map(Cookie::getValue)
            .filter(StringUtils::hasText)
            .findFirst();
    }

    @Override
    public String loginRedirectUri(String redirectUri) {
        if (StringUtils.hasText(redirectUri)) {
            return redirectUri;
        }
        return properties.getKeycloak().getCallbackUri();
    }

    @Override
    public void setSessionCookie(AuthorizationBffSessionEntity session) {
        response().addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(
                properties.getSession().getCookieName(),
                session.sessionId()
            )
            .httpOnly(true)
            .secure(properties.getSession().isSecure())
            .sameSite(properties.getSession().getSameSite())
            .path("/")
            .maxAge(properties.getSession().getExpiresIn())
            .build()
            .toString());
    }

    @Override
    public void expireSessionCookie() {
        response().addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(properties.getSession().getCookieName(), "")
            .httpOnly(true)
            .secure(properties.getSession().isSecure())
            .sameSite(properties.getSession().getSameSite())
            .path("/")
            .maxAge(0)
            .build()
            .toString());
    }

    private HttpServletRequest request() {
        return attributes().getRequest();
    }

    private HttpServletResponse response() {
        return attributes().getResponse();
    }

    private ServletRequestAttributes attributes() {
        return (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    }
}
