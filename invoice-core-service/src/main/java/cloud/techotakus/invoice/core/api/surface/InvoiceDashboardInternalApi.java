package cloud.techotakus.invoice.core.api.surface;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.core.api.model.InvoiceDashboardEmployeeSummaryModel;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/internal/v1/dashboard")
public interface InvoiceDashboardInternalApi {

    @GetMapping("/employee-summary")
    ResponseEntity<RestResponse<InvoiceDashboardEmployeeSummaryModel>> employeeSummary(
            @NotBlank @RequestParam String tenantId,
            @NotBlank @RequestParam String userId
    );
}
