package cloud.techotakus.invoice.core.infra.client.common;

import cloud.techotakus.common.pojo.http.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "invoice-common-service",
        url = "${invoice.core.common-service.base-url:http://localhost:8088}"
)
public interface CoreNonceClient {

    @PostMapping("/internal/v1/nonces/verify-consume")
    RestResponse<CoreNonceVerifyResponse> verifyAndConsume(@RequestBody CoreNonceVerifyRequest request);
}
