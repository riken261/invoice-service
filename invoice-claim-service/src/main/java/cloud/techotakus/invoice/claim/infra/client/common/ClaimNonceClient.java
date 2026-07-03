package cloud.techotakus.invoice.claim.infra.client.common;

import cloud.techotakus.common.pojo.http.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "claimCommonNonceClient",
        url = "${invoice.claim.common-service.base-url:http://localhost:8088}"
)
public interface ClaimNonceClient {

    @PostMapping("/internal/v1/nonces/verify-consume")
    RestResponse<ClaimNonceVerifyResponse> verifyAndConsume(@RequestBody ClaimNonceVerifyRequest request);
}
