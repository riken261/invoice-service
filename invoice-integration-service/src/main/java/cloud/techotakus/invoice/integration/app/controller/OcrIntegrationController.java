package cloud.techotakus.invoice.integration.app.controller;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.integration.api.model.OcrRecognizeRequestModel;
import cloud.techotakus.invoice.integration.api.model.OcrRecognizeResponseModel;
import cloud.techotakus.invoice.integration.api.model.OcrVatVerificationRequestModel;
import cloud.techotakus.invoice.integration.api.model.OcrVatVerificationResponseModel;
import cloud.techotakus.invoice.integration.api.surface.OcrIntegrationApi;
import cloud.techotakus.invoice.integration.app.mapstruct.OcrIntegrationMapstruct;
import cloud.techotakus.invoice.integration.domain.usecase.OcrIntegrationUseCase;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OcrIntegrationController implements OcrIntegrationApi {

    @Resource
    private OcrIntegrationUseCase useCase;

    @Resource
    private OcrIntegrationMapstruct mapstruct;

    @Override
    public ResponseEntity<RestResponse<OcrRecognizeResponseModel>> recognize(OcrRecognizeRequestModel request) {
        return ResponseEntity.ok(RestResponse.success(mapstruct.map(useCase.recognize(mapstruct.map(request)))));
    }

    @Override
    public ResponseEntity<RestResponse<OcrVatVerificationResponseModel>> verify(OcrVatVerificationRequestModel request) {
        return ResponseEntity.ok(RestResponse.success(mapstruct.map(useCase.verify(mapstruct.map(request)))));
    }

}
