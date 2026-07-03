package cloud.techotakus.invoice.claim.api.surface;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.claim.api.model.ClaimDashboardEmployeeSummaryModel;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/internal/v1/dashboard")
public interface ClaimDashboardInternalApi {

    @GetMapping("/employee-summary")
    ResponseEntity<RestResponse<ClaimDashboardEmployeeSummaryModel>> employeeSummary(
            @NotBlank @RequestParam String tenantId,
            @NotBlank @RequestParam String userId
    );
}
