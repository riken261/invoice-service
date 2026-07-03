package cloud.techotakus.invoice.review.domain.usecase;

import cloud.techotakus.invoice.review.api.model.ReviewDashboardFinanceSummaryModel;
import cloud.techotakus.invoice.review.domain.repository.ReviewDashboardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewDashboardUseCaseTest {

    @Test
    void financeSummaryUsesOldFinanceDashboardStatuses() {
        FakeRepository repository = new FakeRepository();
        ReviewDashboardUseCase useCase = new ReviewDashboardUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);

        ReviewDashboardFinanceSummaryModel summary = useCase.financeSummary("tenant_1", "reviewer_1");

        assertThat(repository.invoiceStatuses).containsExactly("USER_CONFIRMED", "MANUAL_REVIEW_REQUIRED");
        assertThat(repository.duplicateStatuses).containsExactly("POSSIBLE_DUPLICATE", "CONFIRMED_DUPLICATE");
        assertThat(repository.claimStatuses).containsExactly("SUBMITTED", "FINANCE_REVIEWING");
        assertThat(summary.pendingInvoiceCount()).isEqualTo(2);
        assertThat(summary.duplicateInvoiceCount()).isEqualTo(3);
        assertThat(summary.pendingClaimCount()).isEqualTo(5);
        assertThat(summary.averageWaitingHours()).isEqualTo("1.25");
    }

    private static final class FakeRepository implements ReviewDashboardRepository {
        private String[] invoiceStatuses;
        private String[] duplicateStatuses;
        private String[] claimStatuses;

        @Override
        public long countInvoicesByStatus(String tenantId, String... statuses) {
            invoiceStatuses = statuses;
            return 2;
        }

        @Override
        public long countInvoicesByDuplicateStatus(String tenantId, String... statuses) {
            duplicateStatuses = statuses;
            return 3;
        }

        @Override
        public long countClaimsByStatus(String tenantId, String... statuses) {
            claimStatuses = statuses;
            return 5;
        }

        @Override
        public String averagePendingClaimWaitingHours(String tenantId) {
            return "1.25";
        }
    }
}
