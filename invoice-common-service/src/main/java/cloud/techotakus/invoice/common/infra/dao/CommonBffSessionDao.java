package cloud.techotakus.invoice.common.infra.dao;

import cloud.techotakus.invoice.common.domain.entity.CommonBffSessionEntity;
import cloud.techotakus.invoice.common.domain.repository.CommonBffSessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;

@Repository
public class CommonBffSessionDao implements CommonBffSessionRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private final String redisKeyPrefix;

    public CommonBffSessionDao(
            StringRedisTemplate redisTemplate,
            @Value("${invoice.common.session.redis-key-prefix:invoice:gateway:bff-session:}") String redisKeyPrefix
    ) {
        this.redisTemplate = redisTemplate;
        this.redisKeyPrefix = redisKeyPrefix;
    }

    @Override
    public CommonBffSessionEntity find(String sessionId) {
        String value = redisTemplate.opsForValue().get(key(sessionId));
        if (value == null) {
            return null;
        }
        CommonBffSessionEntity session = read(value);
        if (session.expired(Instant.now())) {
            return null;
        }
        redisTemplate.expire(key(sessionId), ttl(session));
        return session;
    }

    private String key(String sessionId) {
        return redisKeyPrefix + sessionId;
    }

    private Duration ttl(CommonBffSessionEntity session) {
        Duration ttl = Duration.between(Instant.now(), session.expiresAt());
        return ttl.isNegative() || ttl.isZero() ? Duration.ofSeconds(1) : ttl;
    }

    private CommonBffSessionEntity read(String value) {
        try {
            return objectMapper.readValue(value, CommonBffSessionEntity.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to deserialize bff session", exception);
        }
    }
}
