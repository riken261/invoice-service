package cloud.techotakus.invoice.gateway.auth;

import cloud.techotakus.invoice.gateway.config.GatewaySessionProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewaySessionContextFilter implements GlobalFilter, Ordered {

    public static final String SESSION_ID_HEADER = "X-Invoice-Session-Id";
    public static final String TENANT_ID_HEADER = "X-Invoice-Tenant-Id";
    public static final String TENANT_CODE_HEADER = "X-Invoice-Tenant-Code";
    public static final String USER_ID_HEADER = "X-Invoice-User-Id";
    public static final String USERNAME_HEADER = "X-Invoice-Username";
    public static final String EMAIL_HEADER = "X-Invoice-Email";
    public static final String ROLES_HEADER = "X-Invoice-Roles";
    public static final String PERMISSIONS_HEADER = "X-Invoice-Permissions";

    private static final Logger log = LoggerFactory.getLogger(GatewaySessionContextFilter.class);
    private static final List<String> CONTEXT_HEADERS = List.of(
        SESSION_ID_HEADER,
        TENANT_ID_HEADER,
        TENANT_CODE_HEADER,
        USER_ID_HEADER,
        USERNAME_HEADER,
        EMAIL_HEADER,
        ROLES_HEADER,
        PERMISSIONS_HEADER
    );

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private final GatewaySessionProperties properties;

    public GatewaySessionContextFilter(
        ReactiveStringRedisTemplate redisTemplate,
        GatewaySessionProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        ServerHttpRequest sanitizedRequest = sanitizeContextHeaders(exchange.getRequest());
        ServerWebExchange sanitizedExchange = exchange.mutate().request(sanitizedRequest).build();

        if (!requiresSession(path) || publicBffPath(path)) {
            return chain.filter(sanitizedExchange);
        }

        String sessionId = sessionCookieValue(sanitizedExchange);
        if (!StringUtils.hasText(sessionId)) {
            return unauthorized(sanitizedExchange);
        }

        return redisTemplate.opsForValue()
            .get(sessionKey(sessionId))
            .flatMap(value -> readSession(value)
                .filter(session -> !session.expired(Instant.now()))
                .map(Mono::just)
                .orElseGet(() -> Mono.empty()))
            .switchIfEmpty(Mono.defer(() -> Mono.error(new UnauthorizedSessionException())))
            .flatMap(session -> refreshSession(session)
                .then(chain.filter(sanitizedExchange.mutate()
                    .request(injectContextHeaders(sanitizedRequest, session))
                    .build())))
            .onErrorResume(UnauthorizedSessionException.class, ignored -> unauthorized(sanitizedExchange));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private boolean requiresSession(String path) {
        String[] segments = path.split("/");
        return segments.length >= 4
            && StringUtils.hasText(segments[1])
            && "bff".equals(segments[2])
            && "v1".equals(segments[3]);
    }

    private boolean publicBffPath(String path) {
        return properties.getPublicBffSuffixes().stream().anyMatch(path::endsWith);
    }

    private ServerHttpRequest sanitizeContextHeaders(ServerHttpRequest request) {
        return request.mutate()
            .headers(headers -> CONTEXT_HEADERS.forEach(headers::remove))
            .build();
    }

    private ServerHttpRequest injectContextHeaders(ServerHttpRequest request, GatewayBffSession session) {
        return request.mutate()
            .headers(headers -> {
                put(headers, SESSION_ID_HEADER, session.sessionId());
                put(headers, TENANT_ID_HEADER, session.tenantId());
                put(headers, TENANT_CODE_HEADER, session.tenantCode());
                put(headers, USER_ID_HEADER, session.userId());
                put(headers, USERNAME_HEADER, session.username());
                put(headers, EMAIL_HEADER, session.email());
                put(headers, ROLES_HEADER, join(session.roles()));
                put(headers, PERMISSIONS_HEADER, join(session.permissions()));
            })
            .build();
    }

    private void put(HttpHeaders headers, String name, String value) {
        if (StringUtils.hasText(value)) {
            headers.set(name, value);
        }
    }

    private String join(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return String.join(",", values);
    }

    private String sessionCookieValue(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(properties.getCookieName());
        return cookie == null ? null : cookie.getValue();
    }

    private String sessionKey(String sessionId) {
        return properties.getRedisKeyPrefix() + sessionId;
    }

    private Mono<Boolean> refreshSession(GatewayBffSession session) {
        Duration ttl = Duration.between(Instant.now(), session.expiresAt());
        if (!ttl.isPositive()) {
            return Mono.just(false);
        }
        return redisTemplate.expire(sessionKey(session.sessionId()), ttl);
    }

    private java.util.Optional<GatewayBffSession> readSession(String value) {
        try {
            return java.util.Optional.of(objectMapper.readValue(value, GatewayBffSession.class));
        } catch (JsonProcessingException exception) {
            log.warn("Failed to deserialize BFF session from Redis", exception);
            return java.util.Optional.empty();
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.empty();
        }
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json");
        byte[] bytes = "{\"success\":false,\"message\":\"not logged in\"}".getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private static final class UnauthorizedSessionException extends RuntimeException {
    }
}
