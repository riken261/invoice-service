package cloud.techotakus.invoice.common.infra.dao;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardClaimSummaryEntity;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardFinanceSummaryEntity;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardInvoiceSummaryEntity;
import cloud.techotakus.invoice.common.domain.repository.CommonDashboardSummaryRepository;
import cloud.techotakus.invoice.common.infra.client.dashboard.CommonClaimDashboardClient;
import cloud.techotakus.invoice.common.infra.client.dashboard.CommonCoreDashboardClient;
import cloud.techotakus.invoice.common.infra.client.dashboard.CommonReviewDashboardClient;
import cloud.techotakus.invoice.common.infra.client.dashboard.model.CommonDashboardClaimSummaryResponse;
import cloud.techotakus.invoice.common.infra.client.dashboard.model.CommonDashboardFinanceSummaryResponse;
import cloud.techotakus.invoice.common.infra.client.dashboard.model.CommonDashboardInvoiceSummaryResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class CommonDashboardSummaryDao implements CommonDashboardSummaryRepository {

    @Resource
    private CommonCoreDashboardClient coreDashboardClient;

    @Resource
    private CommonClaimDashboardClient claimDashboardClient;

    @Resource
    private CommonReviewDashboardClient reviewDashboardClient;

    @Override
    public CommonDashboardInvoiceSummaryEntity invoiceSummary(String tenantId, String userId) {
        CommonDashboardInvoiceSummaryResponse response = unwrap(
                coreDashboardClient.employeeSummary(tenantId, userId),
                "Invoice dashboard summary failed"
        );
        return new CommonDashboardInvoiceSummaryEntity(
                response.ocrProcessingCount(),
                response.ocrConfirmRequiredCount(),
                response.manualInputRequiredCount(),
                response.rejectedInvoiceCount()
        );
    }

    @Override
    public CommonDashboardClaimSummaryEntity claimSummary(String tenantId, String userId) {
        CommonDashboardClaimSummaryResponse response = unwrap(
                claimDashboardClient.employeeSummary(tenantId, userId),
                "Claim dashboard summary failed"
        );
        return new CommonDashboardClaimSummaryEntity(
                response.draftClaimCount(),
                response.rejectedClaimCount()
        );
    }

    @Override
    public CommonDashboardFinanceSummaryEntity financeSummary(String tenantId, String reviewerId) {
        CommonDashboardFinanceSummaryResponse response = unwrap(
                reviewDashboardClient.financeSummary(tenantId, reviewerId),
                "Finance dashboard summary failed"
        );
        return new CommonDashboardFinanceSummaryEntity(
                response.pendingInvoiceCount(),
                response.duplicateInvoiceCount(),
                response.pendingClaimCount(),
                response.averageWaitingHours()
        );
    }

    private static <T> T unwrap(RestResponse<T> response, String defaultMessage) {
        if (response != null && response.isSuccess()) {
            return response.getData();
        }
        String message = response == null || !StringUtils.hasText(response.getError())
                ? defaultMessage
                : response.getError();
        throw new ServiceException(message, ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE);
    }
}
