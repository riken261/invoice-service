package cloud.techotakus.invoice.integration.api.surface;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.integration.api.model.OcrRecognizeRequestModel;
import cloud.techotakus.invoice.integration.api.model.OcrRecognizeResponseModel;
import cloud.techotakus.invoice.integration.api.model.OcrVatVerificationRequestModel;
import cloud.techotakus.invoice.integration.api.model.OcrVatVerificationResponseModel;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/internal/v1/integrations/ocr")
public interface OcrIntegrationApi {

    @PostMapping("/recognize")
    ResponseEntity<RestResponse<OcrRecognizeResponseModel>> recognize(
            @Valid @RequestBody OcrRecognizeRequestModel request
    );

    @PostMapping("/vat-verify")
    ResponseEntity<RestResponse<OcrVatVerificationResponseModel>> verify(
            @Valid @RequestBody OcrVatVerificationRequestModel request
    );
}
