package cloud.techotakus.invoice.common.infra.client.dashboard;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.common.infra.client.dashboard.model.CommonDashboardFinanceSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "commonReviewDashboardClient", url = "${invoice.common.review-service.base-url}")
public interface CommonReviewDashboardClient {

    @GetMapping("/internal/v1/dashboard/finance-summary")
    RestResponse<CommonDashboardFinanceSummaryResponse> financeSummary(
            @RequestParam String tenantId,
            @RequestParam String reviewerId
    );
}
