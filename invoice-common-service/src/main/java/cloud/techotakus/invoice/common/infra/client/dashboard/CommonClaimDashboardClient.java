package cloud.techotakus.invoice.common.infra.client.dashboard;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.common.infra.client.dashboard.model.CommonDashboardClaimSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "commonClaimDashboardClient", url = "${invoice.common.claim-service.base-url}")
public interface CommonClaimDashboardClient {

    @GetMapping("/internal/v1/dashboard/employee-summary")
    RestResponse<CommonDashboardClaimSummaryResponse> employeeSummary(
            @RequestParam String tenantId,
            @RequestParam String userId
    );
}
