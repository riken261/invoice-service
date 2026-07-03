package cloud.techotakus.invoice.review.api.surface;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.review.api.model.ReviewDashboardFinanceSummaryModel;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/internal/v1/dashboard")
public interface ReviewDashboardInternalApi {

    @GetMapping("/finance-summary")
    ResponseEntity<RestResponse<ReviewDashboardFinanceSummaryModel>> financeSummary(
            @NotBlank @RequestParam String tenantId,
            @NotBlank @RequestParam String reviewerId
    );
}
