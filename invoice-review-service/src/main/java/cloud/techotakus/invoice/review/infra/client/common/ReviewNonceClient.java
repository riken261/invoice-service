package cloud.techotakus.invoice.review.infra.client.common;

import cloud.techotakus.common.pojo.http.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "reviewCommonNonceClient",
        url = "${invoice.review.common-service.base-url:http://localhost:8088}"
)
public interface ReviewNonceClient {

    @PostMapping("/internal/v1/nonces/verify-consume")
    RestResponse<ReviewNonceVerifyResponse> verifyAndConsume(@RequestBody ReviewNonceVerifyRequest request);
}
