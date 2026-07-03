package cloud.techotakus.invoice.review.app.controller;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.review.api.model.ReviewDashboardFinanceSummaryModel;
import cloud.techotakus.invoice.review.api.surface.ReviewDashboardInternalApi;
import cloud.techotakus.invoice.review.domain.usecase.ReviewDashboardUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Review Dashboard Internal", description = "Review dashboard internal APIs")
public class ReviewDashboardInternalController implements ReviewDashboardInternalApi {

    @Resource
    private ReviewDashboardUseCase useCase;

    @Override
    public ResponseEntity<RestResponse<ReviewDashboardFinanceSummaryModel>> financeSummary(String tenantId, String reviewerId) {
        return ResponseEntity.ok(RestResponse.success(useCase.financeSummary(tenantId, reviewerId)));
    }
}
