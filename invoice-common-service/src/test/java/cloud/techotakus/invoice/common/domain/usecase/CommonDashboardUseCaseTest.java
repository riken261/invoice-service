package cloud.techotakus.invoice.common.domain.usecase;

import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardActionEntity;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardClaimSummaryEntity;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardFinanceSummaryEntity;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardInvoiceSummaryEntity;
import cloud.techotakus.invoice.common.domain.repository.CommonDashboardRepository;
import cloud.techotakus.invoice.common.domain.repository.CommonDashboardSummaryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommonDashboardUseCaseTest {

    @Test
    void financeSummaryRequiresFinanceReviewPermission() {
        CommonDashboardUseCase useCase = new CommonDashboardUseCase(new FakeDashboardRepository(), new FakeSummaryRepository());

        assertThatThrownBy(() -> useCase.financeSummary("tenant_1", "reviewer_1", "invoice:view:self"))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    void financeSummaryDelegatesToReviewSummaryRepository() {
        FakeSummaryRepository summaryRepository = new FakeSummaryRepository();
        CommonDashboardUseCase useCase = new CommonDashboardUseCase(new FakeDashboardRepository(), summaryRepository);

        CommonDashboardFinanceSummaryEntity summary = useCase.financeSummary(
                "tenant_1",
                "reviewer_1",
                "claim:finance-review"
        );

        assertThat(summaryRepository.financeTenantId).isEqualTo("tenant_1");
        assertThat(summaryRepository.financeReviewerId).isEqualTo("reviewer_1");
        assertThat(summary.pendingClaimCount()).isEqualTo(5);
        assertThat(summary.averageWaitingHours()).isEqualTo("1.25");
    }

    private static final class FakeDashboardRepository implements CommonDashboardRepository {
        @Override
        public List<CommonDashboardActionEntity> enabledActions() {
            return List.of();
        }
    }

    private static final class FakeSummaryRepository implements CommonDashboardSummaryRepository {
        private String financeTenantId;
        private String financeReviewerId;

        @Override
        public CommonDashboardInvoiceSummaryEntity invoiceSummary(String tenantId, String userId) {
            return new CommonDashboardInvoiceSummaryEntity(0, 0, 0, 0);
        }

        @Override
        public CommonDashboardClaimSummaryEntity claimSummary(String tenantId, String userId) {
            return new CommonDashboardClaimSummaryEntity(0, 0);
        }

        @Override
        public CommonDashboardFinanceSummaryEntity financeSummary(String tenantId, String reviewerId) {
            financeTenantId = tenantId;
            financeReviewerId = reviewerId;
            return new CommonDashboardFinanceSummaryEntity(2, 3, 5, "1.25");
        }
    }
}
