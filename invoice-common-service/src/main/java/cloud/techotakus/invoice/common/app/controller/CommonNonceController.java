package cloud.techotakus.invoice.common.app.controller;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.common.api.model.CommonNonceCreateRequestModel;
import cloud.techotakus.invoice.common.api.model.CommonNonceCreateResponseModel;
import cloud.techotakus.invoice.common.api.model.CommonNonceVerifyRequestModel;
import cloud.techotakus.invoice.common.api.model.CommonNonceVerifyResponseModel;
import cloud.techotakus.invoice.common.api.surface.CommonNonceApi;
import cloud.techotakus.invoice.common.app.mapstruct.CommonNonceMapstruct;
import cloud.techotakus.invoice.common.domain.entity.CommonNonceEntity;
import cloud.techotakus.invoice.common.domain.usecase.CommonNonceUseCase;
import cloud.techotakus.invoice.starters.support.InvoiceGatewayContextResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Nonce", description = "Operation nonce APIs")
public class CommonNonceController implements CommonNonceApi {

    @Resource
    private CommonNonceUseCase useCase;

    @Resource
    private CommonNonceMapstruct mapstruct;

    @Resource
    private InvoiceGatewayContextResolver gatewayContextResolver;

    @Override
    public ResponseEntity<RestResponse<CommonNonceCreateResponseModel>> createNonce(
            CommonNonceCreateRequestModel request,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        CommonNonceEntity entity = mapstruct.map(request);
        entity.setTenantId(context.tenantId());
        entity.setOwnerUserId(context.userId());
        entity.setSessionId(context.sessionId());
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapCreate(useCase.create(entity))));
    }

    @Override
    public ResponseEntity<RestResponse<CommonNonceVerifyResponseModel>> verifyAndConsume(CommonNonceVerifyRequestModel request) {
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapVerify(useCase.verifyAndConsume(mapstruct.map(request)))));
    }
}
