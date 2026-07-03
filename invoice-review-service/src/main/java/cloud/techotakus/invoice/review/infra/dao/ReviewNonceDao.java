package cloud.techotakus.invoice.review.infra.dao;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.review.domain.repository.ReviewNonceRepository;
import cloud.techotakus.invoice.review.infra.client.common.ReviewNonceClient;
import cloud.techotakus.invoice.review.infra.client.common.ReviewNonceVerifyRequest;
import cloud.techotakus.invoice.review.infra.client.common.ReviewNonceVerifyResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class ReviewNonceDao implements ReviewNonceRepository {

    @Resource
    private ReviewNonceClient nonceClient;

    @Override
    public void verifyAndConsume(
            String tenantId,
            String ownerUserId,
            String sessionId,
            String operation,
            String resourceType,
            String resourceId,
            String nonce
    ) {
        RestResponse<ReviewNonceVerifyResponse> response = nonceClient.verifyAndConsume(new ReviewNonceVerifyRequest(
                tenantId,
                ownerUserId,
                sessionId,
                operation,
                resourceType,
                resourceId,
                nonce
        ));
        ReviewNonceVerifyResponse data = unwrap(response);
        if (data == null || !data.valid()) {
            throw new ServiceException("nonce verification failed", ErrorCode.NONCE_MISMATCH);
        }
    }

    private static ReviewNonceVerifyResponse unwrap(RestResponse<ReviewNonceVerifyResponse> response) {
        if (response != null && response.isSuccess()) {
            return response.getData();
        }
        String message = response == null || !StringUtils.hasText(response.getError())
                ? "nonce verification failed"
                : response.getError();
        throw new ServiceException(message, ErrorCode.NONCE_MISMATCH);
    }
}
