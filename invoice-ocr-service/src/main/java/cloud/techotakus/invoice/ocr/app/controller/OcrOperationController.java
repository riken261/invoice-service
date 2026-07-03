package cloud.techotakus.invoice.ocr.app.controller;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.ocr.api.model.OcrRecognizeRequestModel;
import cloud.techotakus.invoice.ocr.api.model.OcrRecognizeResponseModel;
import cloud.techotakus.invoice.ocr.api.model.OcrVatVerificationRequestModel;
import cloud.techotakus.invoice.ocr.api.model.OcrVatVerificationResponseModel;
import cloud.techotakus.invoice.ocr.api.surface.OcrOperationApi;
import cloud.techotakus.invoice.ocr.app.mapstruct.OcrOperationMapstruct;
import cloud.techotakus.invoice.ocr.domain.usecase.OcrOperationUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "OCR", description = "OCR Operation APIs")
public class OcrOperationController implements OcrOperationApi {

    @Resource
    private OcrOperationUseCase useCase;

    @Resource
    private OcrOperationMapstruct mapstruct;

    @Override
    public ResponseEntity<RestResponse<OcrRecognizeResponseModel>> recognize(OcrRecognizeRequestModel request) {
        return ResponseEntity.ok(RestResponse.success(mapstruct.map(useCase.recognize(mapstruct.map(request)))));
    }

    @Override
    public ResponseEntity<RestResponse<OcrVatVerificationResponseModel>> verify(OcrVatVerificationRequestModel request) {
        return ResponseEntity.ok(RestResponse.success(mapstruct.map(useCase.verify(mapstruct.map(request)))));
    }
}
