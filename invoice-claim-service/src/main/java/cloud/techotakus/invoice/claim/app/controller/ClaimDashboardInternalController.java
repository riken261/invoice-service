package cloud.techotakus.invoice.claim.app.controller;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.claim.api.model.ClaimDashboardEmployeeSummaryModel;
import cloud.techotakus.invoice.claim.api.surface.ClaimDashboardInternalApi;
import cloud.techotakus.invoice.claim.domain.usecase.ClaimDashboardUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Claim Dashboard Internal", description = "Claim dashboard internal APIs")
public class ClaimDashboardInternalController implements ClaimDashboardInternalApi {

    @Resource
    private ClaimDashboardUseCase useCase;

    @Override
    public ResponseEntity<RestResponse<ClaimDashboardEmployeeSummaryModel>> employeeSummary(String tenantId, String userId) {
        return ResponseEntity.ok(RestResponse.success(useCase.employeeSummary(tenantId, userId)));
    }
}
