package cloud.techotakus.invoice.common.domain.usecase;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.common.domain.entity.CommonNonceEntity;
import cloud.techotakus.invoice.common.domain.repository.CommonNonceRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class CommonNonceUseCase {

    @Resource
    private CommonNonceRepository repository;

    @Value("${invoice.common.nonce.expires-seconds:300}")
    private long expiresSeconds;

    public CommonNonceEntity create(CommonNonceEntity request) {
        validateCreate(request);
        CommonNonceEntity nonce = new CommonNonceEntity();
        nonce.setTenantId(request.getTenantId().trim());
        nonce.setOwnerUserId(request.getOwnerUserId().trim());
        nonce.setSessionId(request.getSessionId().trim());
        nonce.setOperation(request.getOperation().trim());
        nonce.setResourceType(trimToNull(request.getResourceType()));
        nonce.setResourceId(trimToNull(request.getResourceId()));
        nonce.setNonce("nonce_" + UUID.randomUUID().toString().replace("-", ""));
        nonce.setExpiresAt(OffsetDateTime.now().plusSeconds(expiresSeconds));
        repository.save(nonce);
        return nonce;
    }

    public CommonNonceEntity verifyAndConsume(CommonNonceEntity request) {
        validateVerify(request);
        CommonNonceEntity stored = repository.consume(request.getNonce().trim());
        if (stored == null) {
            throw new ServiceException("nonce was already used or does not exist", ErrorCode.NONCE_USED);
        }
        if (OffsetDateTime.now().isAfter(stored.getExpiresAt())) {
            throw new ServiceException("nonce expired", ErrorCode.NONCE_EXPIRED);
        }
        if (!matches(stored, request)) {
            throw new ServiceException("nonce does not match operation context", ErrorCode.NONCE_MISMATCH);
        }
        return stored;
    }

    private void validateCreate(CommonNonceEntity request) {
        if (request == null) {
            throw new ServiceException("nonce request is required", ErrorCode.VALIDATION_ERROR);
        }
        required(request.getTenantId(), "tenantId");
        required(request.getOwnerUserId(), "ownerUserId");
        required(request.getSessionId(), "sessionId");
        required(request.getOperation(), "operation");
    }

    private void validateVerify(CommonNonceEntity request) {
        validateCreate(request);
        required(request.getNonce(), "nonce");
    }

    private static boolean matches(CommonNonceEntity stored, CommonNonceEntity request) {
        return Objects.equals(stored.getTenantId(), request.getTenantId().trim())
                && Objects.equals(stored.getOwnerUserId(), request.getOwnerUserId().trim())
                && Objects.equals(stored.getSessionId(), request.getSessionId().trim())
                && Objects.equals(stored.getOperation(), request.getOperation().trim())
                && Objects.equals(stored.getResourceType(), trimToNull(request.getResourceType()))
                && Objects.equals(stored.getResourceId(), trimToNull(request.getResourceId()));
    }

    private static String required(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ServiceException(fieldName + " is required", ErrorCode.NONCE_REQUIRED);
        }
        return value.trim();
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
