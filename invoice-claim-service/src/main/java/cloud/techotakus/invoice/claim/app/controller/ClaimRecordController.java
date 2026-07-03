package cloud.techotakus.invoice.claim.app.controller;

import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.claim.api.model.ClaimDetailResponseModel;
import cloud.techotakus.invoice.claim.api.model.ClaimRecordResponseModel;
import cloud.techotakus.invoice.claim.api.model.ClaimReviewRecordResponseModel;
import cloud.techotakus.invoice.claim.api.model.ClaimSaveRequestModel;
import cloud.techotakus.invoice.claim.api.model.ClaimSubmitRequestModel;
import cloud.techotakus.invoice.claim.api.model.ClaimSubmitResponseModel;
import cloud.techotakus.invoice.claim.api.surface.ClaimRecordApi;
import cloud.techotakus.invoice.claim.app.mapstruct.ClaimRecordMapstruct;
import cloud.techotakus.invoice.claim.domain.usecase.ClaimRecordUseCase;
import cloud.techotakus.invoice.starters.support.InvoiceGatewayContextResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Claim Record", description = "Claim record BFF APIs")
public class ClaimRecordController implements ClaimRecordApi {

    @Resource
    private ClaimRecordUseCase useCase;

    @Resource
    private ClaimRecordMapstruct mapstruct;

    @Resource
    private InvoiceGatewayContextResolver gatewayContextResolver;

    @Override
    public ResponseEntity<PageResponse<ClaimDetailResponseModel>> claims(
            int page,
            int pageSize,
            HttpServletRequest request
    ) {
        var context = gatewayContextResolver.currentContext(request);
        var result = useCase.claims(context.tenantId(), context.userId(), page, pageSize);
        return ResponseEntity.ok(PageResponse.success(
                result.getItems().stream().map(mapstruct::mapDetail).toList(),
                result.getPage(),
                result.getPageSize(),
                result.getTotal()
        ));
    }

    @Override
    public ResponseEntity<PageResponse<ClaimRecordResponseModel>> list(
            int page,
            int pageSize,
            HttpServletRequest request
    ) {
        var context = gatewayContextResolver.currentContext(request);
        var result = useCase.list(context.tenantId(), context.userId(), page, pageSize);
        return ResponseEntity.ok(PageResponse.success(
                result.getItems().stream().map(mapstruct::map).toList(),
                result.getPage(),
                result.getPageSize(),
                result.getTotal()
        ));
    }

    @Override
    public ResponseEntity<RestResponse<ClaimDetailResponseModel>> getOne(
            String claimId,
            HttpServletRequest request
    ) {
        var context = gatewayContextResolver.currentContext(request);
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapDetail(
                useCase.getOne(context.tenantId(), context.userId(), claimId)
        )));
    }

    @Override
    public ResponseEntity<RestResponse<List<ClaimReviewRecordResponseModel>>> reviewRecords(
            String claimId,
            HttpServletRequest request
    ) {
        var context = gatewayContextResolver.currentContext(request);
        return ResponseEntity.ok(RestResponse.success(
                useCase.reviewRecords(context.tenantId(), context.userId(), claimId)
                        .stream()
                        .map(mapstruct::mapReviewRecord)
                        .toList()
        ));
    }

    @Override
    public ResponseEntity<RestResponse<ClaimSubmitResponseModel>> save(
            ClaimSaveRequestModel request,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        var entity = mapstruct.map(request);
        entity.setTenantId(context.tenantId());
        entity.setApplicantUserId(context.userId());
        entity.setSessionId(context.sessionId());
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapSubmit(useCase.save(entity))));
    }

    @Override
    public ResponseEntity<RestResponse<ClaimSubmitResponseModel>> update(
            String claimId,
            ClaimSaveRequestModel request,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        var entity = mapstruct.map(request);
        entity.setId(claimId);
        entity.setTenantId(context.tenantId());
        entity.setApplicantUserId(context.userId());
        entity.setSessionId(context.sessionId());
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapSubmit(useCase.updateDraft(entity))));
    }

    @Override
    public ResponseEntity<RestResponse<ClaimSubmitResponseModel>> submit(
            String claimId,
            ClaimSubmitRequestModel request,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        var entity = mapstruct.map(request);
        entity.setId(claimId);
        entity.setTenantId(context.tenantId());
        entity.setApplicantUserId(context.userId());
        entity.setSessionId(context.sessionId());
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapSubmit(useCase.submit(entity))));
    }
}
