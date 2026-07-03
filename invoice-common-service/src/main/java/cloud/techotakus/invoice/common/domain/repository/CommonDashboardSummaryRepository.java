package cloud.techotakus.invoice.common.domain.repository;

import cloud.techotakus.invoice.common.domain.entity.CommonDashboardClaimSummaryEntity;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardFinanceSummaryEntity;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardInvoiceSummaryEntity;

public interface CommonDashboardSummaryRepository {

    CommonDashboardInvoiceSummaryEntity invoiceSummary(String tenantId, String userId);

    CommonDashboardClaimSummaryEntity claimSummary(String tenantId, String userId);

    CommonDashboardFinanceSummaryEntity financeSummary(String tenantId, String reviewerId);
}
