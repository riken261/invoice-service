package cloud.techotakus.invoice.core.infra.dao;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.core.domain.repository.InvoiceNonceRepository;
import cloud.techotakus.invoice.core.infra.client.common.CoreNonceClient;
import cloud.techotakus.invoice.core.infra.client.common.CoreNonceVerifyRequest;
import cloud.techotakus.invoice.core.infra.client.common.CoreNonceVerifyResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class InvoiceNonceDao implements InvoiceNonceRepository {

    @Resource
    private CoreNonceClient nonceClient;

    @Override
    public void verifyAndConsume(String tenantId, String ownerUserId, String sessionId, String operation, String nonce) {
        verifyAndConsume(tenantId, ownerUserId, sessionId, operation, null, nonce);
    }

    @Override
    public void verifyAndConsume(String tenantId, String ownerUserId, String sessionId, String operation, String resourceId, String nonce) {
        RestResponse<CoreNonceVerifyResponse> response = nonceClient.verifyAndConsume(new CoreNonceVerifyRequest(
                tenantId,
                ownerUserId,
                sessionId,
                operation,
                "INVOICE",
                resourceId,
                nonce
        ));
        CoreNonceVerifyResponse data = unwrap(response);
        if (data == null || !data.valid()) {
            throw new ServiceException("nonce verification failed", ErrorCode.NONCE_MISMATCH);
        }
    }

    private static CoreNonceVerifyResponse unwrap(RestResponse<CoreNonceVerifyResponse> response) {
        if (response != null && response.isSuccess()) {
            return response.getData();
        }
        String message = response == null || !StringUtils.hasText(response.getError())
                ? "nonce verification failed"
                : response.getError();
        throw new ServiceException(message, ErrorCode.NONCE_MISMATCH);
    }
}
