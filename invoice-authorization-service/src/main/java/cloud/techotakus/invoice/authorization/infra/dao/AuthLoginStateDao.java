package cloud.techotakus.invoice.authorization.infra.dao;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.authorization.config.AuthProperties;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationLoginStateEntity;
import cloud.techotakus.invoice.authorization.domain.repository.AuthLoginStateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthLoginStateDao implements AuthLoginStateRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private final AuthProperties properties;

    public AuthLoginStateDao(StringRedisTemplate redisTemplate, AuthProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public void save(AuthorizationLoginStateEntity entity) {
        redisTemplate.opsForValue().set(key(entity.state()), write(entity), ttl(entity.expiresAt()));
    }

    @Override
    public Optional<AuthorizationLoginStateEntity> consume(String state) {
        String value = redisTemplate.opsForValue().getAndDelete(key(state));
        if (value == null) {
            return Optional.empty();
        }
        AuthorizationLoginStateEntity entity = read(value);
        if (entity.expired(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    private String key(String state) {
        return properties.getLoginState().getRedisKeyPrefix() + state;
    }

    private Duration ttl(Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        return ttl.isPositive() ? ttl : Duration.ofSeconds(1);
    }

    private String write(AuthorizationLoginStateEntity entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException exception) {
            throw new ServiceException("failed to serialize login state", ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE);
        }
    }

    private AuthorizationLoginStateEntity read(String value) {
        try {
            return objectMapper.readValue(value, AuthorizationLoginStateEntity.class);
        } catch (JsonProcessingException exception) {
            throw new ServiceException("failed to deserialize login state", ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE);
        }
    }
}
