package cloud.techotakus.invoice.common.infra.dao;

import cloud.techotakus.invoice.common.domain.entity.CommonNonceEntity;
import cloud.techotakus.invoice.common.domain.repository.CommonNonceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.OffsetDateTime;

@Repository
public class CommonNonceDao implements CommonNonceRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private final String redisKeyPrefix;

    public CommonNonceDao(
            StringRedisTemplate redisTemplate,
            @Value("${invoice.common.nonce.redis-key-prefix:invoice:common:nonce:}") String redisKeyPrefix
    ) {
        this.redisTemplate = redisTemplate;
        this.redisKeyPrefix = redisKeyPrefix;
    }

    @Override
    public void save(CommonNonceEntity nonce) {
        redisTemplate.opsForValue().set(key(nonce.getNonce()), write(nonce), ttl(nonce));
    }

    @Override
    public CommonNonceEntity consume(String nonce) {
        String value = redisTemplate.opsForValue().getAndDelete(key(nonce));
        return value == null ? null : read(value);
    }

    private String key(String nonce) {
        return redisKeyPrefix + nonce;
    }

    private Duration ttl(CommonNonceEntity nonce) {
        Duration ttl = Duration.between(OffsetDateTime.now(), nonce.getExpiresAt());
        return ttl.isNegative() || ttl.isZero() ? Duration.ofSeconds(1) : ttl;
    }

    private String write(CommonNonceEntity nonce) {
        try {
            return objectMapper.writeValueAsString(nonce);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize nonce", exception);
        }
    }

    private CommonNonceEntity read(String value) {
        try {
            return objectMapper.readValue(value, CommonNonceEntity.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to deserialize nonce", exception);
        }
    }
}
