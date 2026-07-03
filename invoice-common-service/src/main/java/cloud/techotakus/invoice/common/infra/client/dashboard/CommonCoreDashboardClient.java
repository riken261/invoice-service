package cloud.techotakus.invoice.common.infra.client.dashboard;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.common.infra.client.dashboard.model.CommonDashboardInvoiceSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "commonCoreDashboardClient", url = "${invoice.common.core-service.base-url}")
public interface CommonCoreDashboardClient {

    @GetMapping("/internal/v1/dashboard/employee-summary")
    RestResponse<CommonDashboardInvoiceSummaryResponse> employeeSummary(
            @RequestParam String tenantId,
            @RequestParam String userId
    );
}
