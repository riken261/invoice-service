package cloud.techotakus.invoice.ocr.infra.client;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.ocr.domain.entity.OcrRecognizeRequestEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrRecognizeResponseEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrVatVerificationRequestEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrVatVerificationResponseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "invoice-integration-service-ocr",
        url = "${invoice.ocr.integration.base-url:http://localhost:8086}"
)
public interface OcrIntegrationClient {

    @PostMapping("/internal/v1/integrations/ocr/recognize")
    RestResponse<OcrRecognizeResponseEntity> recognize(@RequestBody OcrRecognizeRequestEntity request);

    @PostMapping("/internal/v1/integrations/ocr/vat-verify")
    RestResponse<OcrVatVerificationResponseEntity> verify(@RequestBody OcrVatVerificationRequestEntity request);
}
