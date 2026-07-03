package cloud.techotakus.invoice.core.app.controller;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.core.api.model.InvoiceRecognizeBatchResultResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoiceRecognizeResultResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoiceRecognizeUploadRequestModel;
import cloud.techotakus.invoice.core.api.model.InvoiceRecognizeUploadResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoiceConfirmRequestModel;
import cloud.techotakus.invoice.core.api.model.InvoiceConfirmResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoicePreviewResponseModel;
import cloud.techotakus.invoice.core.domain.entity.InvoiceConfirmEntity;
import cloud.techotakus.invoice.core.api.model.InvoiceManualInputSubmitRequestModel;
import cloud.techotakus.invoice.core.domain.entity.InvoiceManualInputSubmitEntity;
import cloud.techotakus.invoice.core.api.surface.InvoiceRecognizeApi;
import cloud.techotakus.invoice.core.app.mapstruct.InvoiceRecognizeMapstruct;
import cloud.techotakus.invoice.core.domain.usecase.InvoiceConfirmUseCase;
import cloud.techotakus.invoice.core.domain.usecase.InvoiceRecognizeUseCase;
import cloud.techotakus.invoice.starters.support.InvoiceGatewayContextResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Invoice Recognition", description = "Invoice recognition BFF APIs")
public class InvoiceRecognizeController implements InvoiceRecognizeApi {

    @Resource
    private InvoiceRecognizeUseCase useCase;

    @Resource
    private InvoiceConfirmUseCase confirmUseCase;

    @Resource
    private InvoiceRecognizeMapstruct mapstruct;

    @Resource
    private InvoiceGatewayContextResolver gatewayContextResolver;

    @Override
    public ResponseEntity<RestResponse<InvoiceRecognizeUploadResponseModel>> recognizeUpload(
            InvoiceRecognizeUploadRequestModel request,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        var entity = mapstruct.map(request);
        entity.setTenantId(context.tenantId());
        entity.setOwnerUserId(context.userId());
        entity.setSessionId(context.sessionId());
        var result = useCase.recognizeUpload(entity);
        return ResponseEntity.ok(RestResponse.success(
                mapstruct.mapUploadResponse(result.getBatchId(), result.getSessions(), useCase.nextPollAfterMs())
        ));
    }

    @Override
    public ResponseEntity<RestResponse<InvoiceRecognizeResultResponseModel>> recognizeResult(String sessionId) {
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapResult(useCase.recognizeResult(sessionId))));
    }

    @Override
    public ResponseEntity<RestResponse<InvoiceRecognizeBatchResultResponseModel>> recognizeResults(String batchId) {
        return ResponseEntity.ok(RestResponse.success(
                mapstruct.mapBatchResult(batchId, useCase.recognizeResults(batchId), useCase.nextPollAfterMs())
        ));
    }

    @Override
    public ResponseEntity<RestResponse<InvoicePreviewResponseModel>> previewSession(
            String recognitionSessionId,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapPreview(
                useCase.previewSession(context.tenantId(), context.userId(), recognitionSessionId)
        )));
    }

    @Override
    public ResponseEntity<RestResponse<InvoiceConfirmResponseModel>> confirmSession(
            String recognitionSessionId,
            InvoiceConfirmRequestModel request,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        InvoiceConfirmEntity entity = new InvoiceConfirmEntity();
        entity.setTenantId(context.tenantId());
        entity.setOwnerUserId(context.userId());
        entity.setSessionId(context.sessionId());
        entity.setRecognitionSessionId(recognitionSessionId);
        entity.setNonce(request.nonce());
        entity.setConfirmedFields(request.confirmedFields());
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapConfirmResponse(confirmUseCase.confirm(entity))));
    }

    @Override
    public ResponseEntity<RestResponse<InvoiceConfirmResponseModel>> submitSessionManualInput(
            String recognitionSessionId,
            InvoiceManualInputSubmitRequestModel request,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        InvoiceManualInputSubmitEntity entity = new InvoiceManualInputSubmitEntity();
        entity.setTenantId(context.tenantId());
        entity.setOwnerUserId(context.userId());
        entity.setSessionId(context.sessionId());
        entity.setRecognitionSessionId(recognitionSessionId);
        entity.setNonce(request.nonce());
        entity.setIdempotencyKey(request.idempotencyKey());
        entity.setFields(request.fields());
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapConfirmResponse(confirmUseCase.submitSessionManualInput(entity))));
    }

    @Override
    public ResponseEntity<RestResponse<InvoiceConfirmResponseModel>> submitManualInput(
            String invoiceId,
            InvoiceManualInputSubmitRequestModel request,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        InvoiceManualInputSubmitEntity entity = new InvoiceManualInputSubmitEntity();
        entity.setTenantId(context.tenantId());
        entity.setOwnerUserId(context.userId());
        entity.setSessionId(context.sessionId());
        entity.setInvoiceId(invoiceId);
        entity.setNonce(request.nonce());
        entity.setIdempotencyKey(request.idempotencyKey());
        entity.setFields(request.fields());
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapConfirmResponse(confirmUseCase.submitManualInput(entity))));
    }
}
