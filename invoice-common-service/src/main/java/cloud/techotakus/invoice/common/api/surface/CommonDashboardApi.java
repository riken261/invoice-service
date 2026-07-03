package cloud.techotakus.invoice.common.api.surface;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.common.api.model.CommonDashboardActionMenusResponseModel;
import cloud.techotakus.invoice.common.api.model.CommonDashboardEmployeeSummaryResponseModel;
import cloud.techotakus.invoice.common.api.model.CommonDashboardFinanceSummaryResponseModel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/bff/v1/dashboard")
public interface CommonDashboardApi {

    @GetMapping("/action-menus")
    ResponseEntity<RestResponse<CommonDashboardActionMenusResponseModel>> actionMenus(
        @RequestHeader(value = "X-Invoice-Permissions", required = false) String permissions
    );

    @GetMapping("/employee-summary")
    ResponseEntity<RestResponse<CommonDashboardEmployeeSummaryResponseModel>> employeeSummary(
        @RequestHeader(value = "X-Invoice-Permissions", required = false) String permissions,
        HttpServletRequest request
    );

    @GetMapping("/finance-summary")
    ResponseEntity<RestResponse<CommonDashboardFinanceSummaryResponseModel>> financeSummary(
        @RequestHeader(value = "X-Invoice-Permissions", required = false) String permissions,
        HttpServletRequest request
    );
}
