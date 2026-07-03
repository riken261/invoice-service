package cloud.techotakus.invoice.authorization.infra.dao;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.authorization.config.AuthProperties;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationBffSessionEntity;
import cloud.techotakus.invoice.authorization.domain.repository.AuthBffSessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthBffSessionDao implements AuthBffSessionRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private final AuthProperties properties;

    public AuthBffSessionDao(StringRedisTemplate redisTemplate, AuthProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public void save(AuthorizationBffSessionEntity entity) {
        redisTemplate.opsForValue().set(key(entity.sessionId()), write(entity), ttl(entity.expiresAt()));
    }

    @Override
    public Optional<AuthorizationBffSessionEntity> find(String sessionId) {
        String value = redisTemplate.opsForValue().get(key(sessionId));
        if (value == null) {
            return Optional.empty();
        }
        AuthorizationBffSessionEntity entity = read(value);
        if (entity.expired(Instant.now())) {
            remove(sessionId);
            return Optional.empty();
        }
        redisTemplate.expire(key(sessionId), ttl(entity.expiresAt()));
        return Optional.of(entity);
    }

    @Override
    public void remove(String sessionId) {
        redisTemplate.delete(key(sessionId));
    }

    private String key(String sessionId) {
        return properties.getSession().getRedisKeyPrefix() + sessionId;
    }

    private Duration ttl(Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        return ttl.isPositive() ? ttl : Duration.ofSeconds(1);
    }

    private String write(AuthorizationBffSessionEntity entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException exception) {
            throw new ServiceException("failed to serialize bff session", ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE);
        }
    }

    private AuthorizationBffSessionEntity read(String value) {
        try {
            return objectMapper.readValue(value, AuthorizationBffSessionEntity.class);
        } catch (JsonProcessingException exception) {
            throw new ServiceException("failed to deserialize bff session", ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE);
        }
    }
}
