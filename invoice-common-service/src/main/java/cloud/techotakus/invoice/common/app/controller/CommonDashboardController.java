package cloud.techotakus.invoice.common.app.controller;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.common.api.model.CommonDashboardActionMenusResponseModel;
import cloud.techotakus.invoice.common.api.model.CommonDashboardEmployeeSummaryResponseModel;
import cloud.techotakus.invoice.common.api.model.CommonDashboardFinanceSummaryResponseModel;
import cloud.techotakus.invoice.common.api.surface.CommonDashboardApi;
import cloud.techotakus.invoice.common.app.mapstruct.CommonDashboardMapstruct;
import cloud.techotakus.invoice.common.domain.usecase.CommonDashboardUseCase;
import cloud.techotakus.invoice.starters.support.InvoiceGatewayContextResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Dashboard", description = "Dashboard BFF APIs")
public class CommonDashboardController implements CommonDashboardApi {

    @Resource
    private CommonDashboardUseCase useCase;

    @Resource
    private CommonDashboardMapstruct mapstruct;

    @Resource
    private InvoiceGatewayContextResolver gatewayContextResolver;

    @Override
    public ResponseEntity<RestResponse<CommonDashboardActionMenusResponseModel>> actionMenus(String permissions) {
        return ResponseEntity.ok(RestResponse.success(mapstruct.map(useCase.actionMenus(permissions))));
    }

    @Override
    public ResponseEntity<RestResponse<CommonDashboardEmployeeSummaryResponseModel>> employeeSummary(
            String permissions,
            HttpServletRequest request
    ) {
        var context = gatewayContextResolver.currentContext(request);
        var summary = useCase.employeeSummary(context.tenantId(), context.userId(), permissions);
        return ResponseEntity.ok(RestResponse.success(new CommonDashboardEmployeeSummaryResponseModel(
                summary.ocrProcessingCount(),
                summary.ocrConfirmRequiredCount(),
                summary.manualInputRequiredCount(),
                summary.rejectedInvoiceCount(),
                summary.draftClaimCount(),
                summary.rejectedClaimCount()
        )));
    }

    @Override
    public ResponseEntity<RestResponse<CommonDashboardFinanceSummaryResponseModel>> financeSummary(
            String permissions,
            HttpServletRequest request
    ) {
        var context = gatewayContextResolver.currentContext(request);
        var summary = useCase.financeSummary(context.tenantId(), context.userId(), permissions);
        return ResponseEntity.ok(RestResponse.success(new CommonDashboardFinanceSummaryResponseModel(
                summary.pendingInvoiceCount(),
                summary.duplicateInvoiceCount(),
                summary.pendingClaimCount(),
                summary.averageWaitingHours()
        )));
    }
}
