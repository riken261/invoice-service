package cloud.techotakus.invoice.ocr.api.surface;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.ocr.api.model.OcrRecognizeRequestModel;
import cloud.techotakus.invoice.ocr.api.model.OcrRecognizeResponseModel;
import cloud.techotakus.invoice.ocr.api.model.OcrVatVerificationRequestModel;
import cloud.techotakus.invoice.ocr.api.model.OcrVatVerificationResponseModel;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/internal/v1/ocr")
public interface OcrOperationApi {

    @PostMapping("/recognize")
    ResponseEntity<RestResponse<OcrRecognizeResponseModel>> recognize(
            @Valid @RequestBody OcrRecognizeRequestModel request
    ) ;

    @PostMapping("/vat-verify")
    ResponseEntity<RestResponse<OcrVatVerificationResponseModel>> verify(
            @Valid @RequestBody OcrVatVerificationRequestModel request
    ) ;
}
