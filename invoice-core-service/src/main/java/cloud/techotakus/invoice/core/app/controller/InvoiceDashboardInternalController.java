package cloud.techotakus.invoice.core.app.controller;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.core.api.model.InvoiceDashboardEmployeeSummaryModel;
import cloud.techotakus.invoice.core.api.surface.InvoiceDashboardInternalApi;
import cloud.techotakus.invoice.core.domain.usecase.InvoiceDashboardUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Invoice Dashboard Internal", description = "Invoice dashboard internal APIs")
public class InvoiceDashboardInternalController implements InvoiceDashboardInternalApi {

    @Resource
    private InvoiceDashboardUseCase useCase;

    @Override
    public ResponseEntity<RestResponse<InvoiceDashboardEmployeeSummaryModel>> employeeSummary(String tenantId, String userId) {
        return ResponseEntity.ok(RestResponse.success(useCase.employeeSummary(tenantId, userId)));
    }
}
