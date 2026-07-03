package cloud.techotakus.invoice.review.app.controller;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.review.api.model.ReviewClaimDecisionRequestModel;
import cloud.techotakus.invoice.review.api.model.ReviewClaimDecisionResponseModel;
import cloud.techotakus.invoice.review.api.model.ReviewClaimDetailResponseModel;
import cloud.techotakus.invoice.review.api.model.ReviewClaimPendingResponseModel;
import cloud.techotakus.invoice.review.api.model.ReviewClaimReviewRecordResponseModel;
import cloud.techotakus.invoice.review.api.surface.ReviewClaimApi;
import cloud.techotakus.invoice.review.api.surface.ReviewClaimRecordApi;
import cloud.techotakus.invoice.review.app.mapstruct.ReviewClaimMapstruct;
import cloud.techotakus.invoice.review.domain.usecase.ReviewClaimUseCase;
import cloud.techotakus.invoice.starters.support.ReviewGatewayContextResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Claim Review", description = "Claim finance review APIs")
public class ReviewClaimController implements ReviewClaimApi, ReviewClaimRecordApi {

    @Resource
    private ReviewClaimUseCase useCase;

    @Resource
    private ReviewClaimMapstruct mapstruct;

    @Resource
    private ReviewGatewayContextResolver gatewayContextResolver;

    @Override
    public ResponseEntity<RestResponse<ReviewClaimDetailResponseModel>> getOne(
            String claimId,
            HttpServletRequest request
    ) {
        var context = gatewayContextResolver.currentContext(request);
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapDetail(
                useCase.getOne(context.tenantId(), claimId)
        )));
    }

    @Override
    public ResponseEntity<RestResponse<ReviewClaimDecisionResponseModel>> reject(
            String claimId,
            ReviewClaimDecisionRequestModel request,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        var entity = mapstruct.map(request);
        entity.setTenantId(context.tenantId());
        entity.setReviewerUserId(context.userId());
        entity.setSessionId(context.sessionId());
        entity.setClaimId(claimId);
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapDecision(useCase.reject(entity))));
    }

    @Override
    public ResponseEntity<RestResponse<ReviewClaimDecisionResponseModel>> approve(
            String claimId,
            ReviewClaimDecisionRequestModel request,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        var entity = mapstruct.map(request);
        entity.setTenantId(context.tenantId());
        entity.setReviewerUserId(context.userId());
        entity.setSessionId(context.sessionId());
        entity.setClaimId(claimId);
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapDecision(useCase.approve(entity))));
    }

    @Override
    public ResponseEntity<PageResponse<ReviewClaimPendingResponseModel>> pending(
            int page,
            int pageSize,
            HttpServletRequest request
    ) {
        var context = gatewayContextResolver.currentContext(request);
        var result = useCase.pending(context.tenantId(), page, pageSize);
        return ResponseEntity.ok(PageResponse.success(
                result.getItems().stream().map(mapstruct::mapPending).toList(),
                result.getPage(),
                result.getPageSize(),
                result.getTotal()
        ));
    }

    @Override
    public ResponseEntity<RestResponse<List<ReviewClaimReviewRecordResponseModel>>> reviewRecords(
            String claimId,
            HttpServletRequest request
    ) {
        var context = gatewayContextResolver.currentContext(request);
        return ResponseEntity.ok(RestResponse.success(
                useCase.reviewRecords(context.tenantId(), claimId)
                        .stream()
                        .map(mapstruct::mapReviewRecord)
                        .toList()
        ));
    }
}
