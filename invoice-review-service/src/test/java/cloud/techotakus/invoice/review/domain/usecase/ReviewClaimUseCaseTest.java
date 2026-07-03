package cloud.techotakus.invoice.review.domain.usecase;

import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimDecisionEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimDetailEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimInvoiceEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimPendingEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimReviewRecordEntity;
import cloud.techotakus.invoice.review.domain.repository.ReviewNonceRepository;
import cloud.techotakus.invoice.review.domain.repository.ReviewClaimRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewClaimUseCaseTest {

    @Test
    void pendingNormalizesPaginationAndTenant() {
        FakeRepository repository = new FakeRepository();
        ReviewClaimUseCase useCase = new ReviewClaimUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);

        useCase.pending(" ichigo ", 0, 500);

        assertThat(repository.tenantId).isEqualTo("ichigo");
        assertThat(repository.page).isEqualTo(1);
        assertThat(repository.pageSize).isEqualTo(100);
    }

    @Test
    void getOneLoadsClaimAndHydratesInvoices() {
        FakeRepository repository = new FakeRepository();
        repository.detail = new ReviewClaimDetailEntity();
        repository.detail.setId("clm_1");
        ReviewClaimInvoiceEntity invoice = new ReviewClaimInvoiceEntity();
        invoice.setId("inv_1");
        invoice.setExpenseCategory("travel");
        repository.invoices = List.of(invoice);
        ReviewClaimUseCase useCase = new ReviewClaimUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);

        ReviewClaimDetailEntity result = useCase.getOne("ichigo", "clm_1");

        assertThat(repository.detailClaimId).isEqualTo("clm_1");
        assertThat(result.getInvoiceIds()).containsExactly("inv_1");
        assertThat(result.getExpenseCategory()).isEqualTo("travel");
    }

    @Test
    void reviewRecordsLoadsClaimBeforeRecords() {
        FakeRepository repository = new FakeRepository();
        repository.detail = new ReviewClaimDetailEntity();
        repository.detail.setId("clm_1");
        ReviewClaimReviewRecordEntity record = new ReviewClaimReviewRecordEntity();
        record.setId("rr_1");
        repository.reviewRecords = List.of(record);
        ReviewClaimUseCase useCase = new ReviewClaimUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);

        List<ReviewClaimReviewRecordEntity> result = useCase.reviewRecords("ichigo", "clm_1");

        assertThat(repository.detailClaimId).isEqualTo("clm_1");
        assertThat(repository.reviewRecordsClaimId).isEqualTo("clm_1");
        assertThat(result).containsExactly(record);
    }

    @Test
    void rejectUpdatesClaimAndWritesReviewRecord() {
        FakeRepository repository = new FakeRepository();
        repository.detail = new ReviewClaimDetailEntity();
        repository.detail.setId("clm_1");
        repository.detail.setStatus("SUBMITTED");
        FakeNonceRepository nonceRepository = new FakeNonceRepository();
        ReviewClaimUseCase useCase = new ReviewClaimUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);
        ReflectionTestUtils.setField(useCase, "nonceRepository", nonceRepository);

        ReviewClaimDecisionEntity request = new ReviewClaimDecisionEntity();
        request.setTenantId("ichigo");
        request.setReviewerUserId("reviewer_1");
        request.setSessionId("session_1");
        request.setClaimId("clm_1");
        request.setNonce("nonce_1");
        request.setIdempotencyKey("review-key");
        request.setComment("missing document");

        ReviewClaimDecisionEntity result = useCase.reject(request);

        assertThat(nonceRepository.operation).isEqualTo("claim.review");
        assertThat(nonceRepository.resourceType).isEqualTo("EXPENSE_CLAIM");
        assertThat(nonceRepository.resourceId).isEqualTo("clm_1");
        assertThat(repository.updatedClaim.getStatus()).isEqualTo("REJECTED");
        assertThat(repository.savedReviewRecord.getBeforeStatus()).isEqualTo("SUBMITTED");
        assertThat(repository.savedReviewRecord.getAfterStatus()).isEqualTo("REJECTED");
        assertThat(repository.savedReviewRecord.getOpinion()).isEqualTo("missing document");
        assertThat(result.getStatus()).isEqualTo("REJECTED");
        assertThat(result.getReviewId()).isEqualTo(repository.savedReviewRecord.getId());
    }

    @Test
    void approveUpdatesClaimAndWritesReviewRecord() {
        FakeRepository repository = new FakeRepository();
        repository.detail = new ReviewClaimDetailEntity();
        repository.detail.setId("clm_1");
        repository.detail.setStatus("SUBMITTED");
        FakeNonceRepository nonceRepository = new FakeNonceRepository();
        ReviewClaimUseCase useCase = new ReviewClaimUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);
        ReflectionTestUtils.setField(useCase, "nonceRepository", nonceRepository);

        ReviewClaimDecisionEntity request = new ReviewClaimDecisionEntity();
        request.setTenantId("ichigo");
        request.setReviewerUserId("reviewer_1");
        request.setSessionId("session_1");
        request.setClaimId("clm_1");
        request.setNonce("nonce_1");
        request.setIdempotencyKey("review-key");
        request.setComment("ok");

        ReviewClaimDecisionEntity result = useCase.approve(request);

        assertThat(nonceRepository.operation).isEqualTo("claim.review");
        assertThat(repository.updatedClaim.getStatus()).isEqualTo("APPROVED");
        assertThat(repository.updatedClaim.getApprovedAt()).isNotNull();
        assertThat(repository.savedReviewRecord.getAction()).isEqualTo("APPROVE");
        assertThat(repository.savedReviewRecord.getBeforeStatus()).isEqualTo("SUBMITTED");
        assertThat(repository.savedReviewRecord.getAfterStatus()).isEqualTo("APPROVED");
        assertThat(result.getStatus()).isEqualTo("APPROVED");
    }

    private static final class FakeNonceRepository implements ReviewNonceRepository {
        private String operation;
        private String resourceType;
        private String resourceId;

        @Override
        public void verifyAndConsume(
                String tenantId,
                String ownerUserId,
                String sessionId,
                String operation,
                String resourceType,
                String resourceId,
                String nonce
        ) {
            this.operation = operation;
            this.resourceType = resourceType;
            this.resourceId = resourceId;
        }
    }

    private static final class FakeRepository implements ReviewClaimRepository {
        private String tenantId;
        private int page;
        private int pageSize;
        private ReviewClaimDetailEntity detail;
        private String detailClaimId;
        private List<ReviewClaimInvoiceEntity> invoices = List.of();
        private List<ReviewClaimReviewRecordEntity> reviewRecords = List.of();
        private String reviewRecordsClaimId;
        private ReviewClaimDetailEntity updatedClaim;
        private ReviewClaimReviewRecordEntity savedReviewRecord;

        @Override
        public PageResponse<ReviewClaimPendingEntity> pending(String tenantId, int page, int pageSize) {
            this.tenantId = tenantId;
            this.page = page;
            this.pageSize = pageSize;
            return PageResponse.success(List.of(), page, pageSize, 0);
        }

        @Override
        public ReviewClaimDetailEntity findDetail(String tenantId, String claimId) {
            this.detailClaimId = claimId;
            return detail;
        }

        @Override
        public List<ReviewClaimInvoiceEntity> findClaimInvoices(String tenantId, String claimId) {
            return invoices;
        }

        @Override
        public List<ReviewClaimReviewRecordEntity> findReviewRecords(String tenantId, String claimId) {
            this.reviewRecordsClaimId = claimId;
            return reviewRecords;
        }

        @Override
        public void updateClaim(ReviewClaimDetailEntity claim) {
            this.updatedClaim = claim;
        }

        @Override
        public void saveReviewRecord(ReviewClaimReviewRecordEntity record) {
            this.savedReviewRecord = record;
        }
    }
}
