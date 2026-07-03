package cloud.techotakus.invoice.claim.infra.dao;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.claim.domain.repository.ClaimNonceRepository;
import cloud.techotakus.invoice.claim.infra.client.common.ClaimNonceClient;
import cloud.techotakus.invoice.claim.infra.client.common.ClaimNonceVerifyRequest;
import cloud.techotakus.invoice.claim.infra.client.common.ClaimNonceVerifyResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class ClaimNonceDao implements ClaimNonceRepository {

    @Resource
    private ClaimNonceClient nonceClient;

    @Override
    public void verifyAndConsume(String tenantId, String ownerUserId, String sessionId, String operation, String nonce) {
        verifyAndConsume(tenantId, ownerUserId, sessionId, operation, null, nonce);
    }

    @Override
    public void verifyAndConsume(String tenantId, String ownerUserId, String sessionId, String operation, String resourceId, String nonce) {
        RestResponse<ClaimNonceVerifyResponse> response = nonceClient.verifyAndConsume(new ClaimNonceVerifyRequest(
                tenantId,
                ownerUserId,
                sessionId,
                operation,
                "EXPENSE_CLAIM",
                resourceId,
                nonce
        ));
        ClaimNonceVerifyResponse data = unwrap(response);
        if (data == null || !data.valid()) {
            throw new ServiceException("nonce verification failed", ErrorCode.NONCE_MISMATCH);
        }
    }

    private static ClaimNonceVerifyResponse unwrap(RestResponse<ClaimNonceVerifyResponse> response) {
        if (response != null && response.isSuccess()) {
            return response.getData();
        }
        String message = response == null || !StringUtils.hasText(response.getError())
                ? "nonce verification failed"
                : response.getError();
        throw new ServiceException(message, ErrorCode.NONCE_MISMATCH);
    }
}
