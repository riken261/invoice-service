package cloud.techotakus.invoice.core.infra.client.ocr;

import cloud.techotakus.common.pojo.http.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "invoice-ocr-service",
        url = "${invoice.core.ocr-service.base-url:http://localhost:8083}"
)
public interface CoreOcrClient {

    @PostMapping("/internal/v1/ocr/recognize")
    RestResponse<CoreOcrRecognizeResponse> recognize(@RequestBody CoreOcrRecognizeRequest request);
}
