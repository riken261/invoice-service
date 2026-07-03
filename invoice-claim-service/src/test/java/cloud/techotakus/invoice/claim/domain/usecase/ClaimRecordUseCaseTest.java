package cloud.techotakus.invoice.claim.domain.usecase;

import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.claim.domain.entity.ClaimDetailEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimItemEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimRecordEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimReviewRecordEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimSubmitEntity;
import cloud.techotakus.invoice.claim.domain.repository.ClaimNonceRepository;
import cloud.techotakus.invoice.claim.domain.repository.ClaimRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClaimRecordUseCaseTest {

    @Test
    void listNormalizesPageAndPageSize() {
        FakeRepository repository = new FakeRepository();
        ClaimRecordUseCase useCase = new ClaimRecordUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);

        useCase.list(" tenant_1 ", " user_1 ", 0, 500);

        assertThat(repository.tenantId).isEqualTo("tenant_1");
        assertThat(repository.userId).isEqualTo("user_1");
        assertThat(repository.page).isEqualTo(1);
        assertThat(repository.pageSize).isEqualTo(100);
    }

    @Test
    void claimsLoadsUserClaimsAndHydratesInvoices() {
        FakeRepository repository = new FakeRepository();
        ClaimDetailEntity claim = new ClaimDetailEntity();
        claim.setId("clm_1");
        repository.claims = List.of(claim);
        repository.claimInvoices = List.of(invoice("inv_1", "10.50"));
        ClaimRecordUseCase useCase = new ClaimRecordUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);

        PageResponse<ClaimDetailEntity> result = useCase.claims("tenant_1", "user_1", 1, 50);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().getFirst().getInvoiceIds()).containsExactly("inv_1");
        assertThat(repository.claimInvoicesTenantId).isEqualTo("tenant_1");
    }

    @Test
    void saveCreatesDraftClaimAndItemsFromClaimableInvoices() {
        FakeRepository repository = new FakeRepository();
        repository.claimableInvoices = List.of(invoice("inv_1", "10.50"), invoice("inv_2", "2.00"));
        FakeNonceRepository nonceRepository = new FakeNonceRepository();
        ClaimRecordUseCase useCase = new ClaimRecordUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);
        ReflectionTestUtils.setField(useCase, "nonceRepository", nonceRepository);

        ClaimSubmitEntity request = new ClaimSubmitEntity();
        request.setTenantId("tenant_1");
        request.setApplicantUserId("user_1");
        request.setSessionId("session_1");
        request.setNonce("nonce_1");
        request.setIdempotencyKey("claim-key");
        request.setDescription("travel claim");
        request.setExpenseCategory("travel");
        request.setInvoiceIds(List.of("inv_1", "inv_2"));

        ClaimSubmitEntity result = useCase.save(request);

        assertThat(nonceRepository.operation).isEqualTo("claim.save");
        assertThat(repository.savedClaim).isSameAs(result);
        assertThat(result.getStatus()).isEqualTo("DRAFT");
        assertThat(result.getSubmittedAt()).isNull();
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("12.50"));
        assertThat(repository.savedItems).hasSize(2);
        assertThat(repository.savedItems).extracting(ClaimItemEntity::getInvoiceId)
                .containsExactly("inv_1", "inv_2");
        assertThat(repository.savedItems).extracting(ClaimItemEntity::getExpenseCategory)
                .containsOnly("travel");
    }

    @Test
    void submitUpdatesDraftClaimAndVerifiesNonceAgainstClaim() {
        FakeRepository repository = new FakeRepository();
        repository.existingClaim = new ClaimSubmitEntity();
        repository.existingClaim.setId("clm_1");
        repository.existingClaim.setStatus("DRAFT");
        repository.existingClaim.setTenantId("tenant_1");
        repository.existingClaim.setApplicantUserId("user_1");
        FakeNonceRepository nonceRepository = new FakeNonceRepository();
        ClaimRecordUseCase useCase = new ClaimRecordUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);
        ReflectionTestUtils.setField(useCase, "nonceRepository", nonceRepository);

        ClaimSubmitEntity request = new ClaimSubmitEntity();
        request.setId("clm_1");
        request.setTenantId("tenant_1");
        request.setApplicantUserId("user_1");
        request.setSessionId("session_1");
        request.setNonce("nonce_1");
        request.setIdempotencyKey("submit-key");

        ClaimSubmitEntity result = useCase.submit(request);

        assertThat(nonceRepository.operation).isEqualTo("claim.submit");
        assertThat(nonceRepository.resourceId).isEqualTo("clm_1");
        assertThat(repository.updatedClaim).isSameAs(result);
        assertThat(result.getStatus()).isEqualTo("SUBMITTED");
        assertThat(result.getSubmittedAt()).isNotNull();
        assertThat(result.getUpdatedTrace()).isEqualTo("submit-key");
    }

    @Test
    void updateDraftRebuildsClaimItemsAndVerifiesNonceAgainstClaim() {
        FakeRepository repository = new FakeRepository();
        repository.existingClaim = new ClaimSubmitEntity();
        repository.existingClaim.setId("clm_1");
        repository.existingClaim.setStatus("DRAFT");
        repository.existingClaim.setTenantId("tenant_1");
        repository.existingClaim.setApplicantUserId("user_1");
        repository.claimableInvoices = List.of(invoice("inv_2", "20.00"), invoice("inv_3", "5.25"));
        FakeNonceRepository nonceRepository = new FakeNonceRepository();
        ClaimRecordUseCase useCase = new ClaimRecordUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);
        ReflectionTestUtils.setField(useCase, "nonceRepository", nonceRepository);

        ClaimSubmitEntity request = new ClaimSubmitEntity();
        request.setId("clm_1");
        request.setTenantId("tenant_1");
        request.setApplicantUserId("user_1");
        request.setSessionId("session_1");
        request.setNonce("nonce_1");
        request.setIdempotencyKey("update-key");
        request.setDescription("updated draft");
        request.setExpenseCategory("meal");
        request.setInvoiceIds(List.of("inv_2", "inv_3"));

        ClaimSubmitEntity result = useCase.updateDraft(request);

        assertThat(nonceRepository.operation).isEqualTo("claim.save");
        assertThat(nonceRepository.resourceId).isEqualTo("clm_1");
        assertThat(repository.deletedClaimItemsClaimId).isEqualTo("clm_1");
        assertThat(repository.updatedDraftClaim).isSameAs(result);
        assertThat(result.getDescription()).isEqualTo("updated draft");
        assertThat(result.getStatus()).isEqualTo("DRAFT");
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("25.25"));
        assertThat(result.getUpdatedTrace()).isEqualTo("update-key");
        assertThat(repository.savedItems).hasSize(2);
        assertThat(repository.savedItems).extracting(ClaimItemEntity::getInvoiceId)
                .containsExactly("inv_2", "inv_3");
        assertThat(repository.savedItems).extracting(ClaimItemEntity::getExpenseCategory)
                .containsOnly("meal");
    }

    @Test
    void updateDraftAllowsRejectedClaimAndMovesItBackToDraft() {
        FakeRepository repository = new FakeRepository();
        repository.existingClaim = new ClaimSubmitEntity();
        repository.existingClaim.setId("clm_1");
        repository.existingClaim.setStatus("REJECTED");
        repository.existingClaim.setTenantId("tenant_1");
        repository.existingClaim.setApplicantUserId("user_1");
        repository.claimableInvoices = List.of(invoice("inv_1", "10.00"));
        FakeNonceRepository nonceRepository = new FakeNonceRepository();
        ClaimRecordUseCase useCase = new ClaimRecordUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);
        ReflectionTestUtils.setField(useCase, "nonceRepository", nonceRepository);

        ClaimSubmitEntity request = new ClaimSubmitEntity();
        request.setId("clm_1");
        request.setTenantId("tenant_1");
        request.setApplicantUserId("user_1");
        request.setSessionId("session_1");
        request.setNonce("nonce_1");
        request.setDescription("retry");
        request.setExpenseCategory("office");
        request.setInvoiceIds(List.of("inv_1"));

        ClaimSubmitEntity result = useCase.updateDraft(request);

        assertThat(result.getStatus()).isEqualTo("DRAFT");
        assertThat(repository.updatedDraftClaim).isSameAs(result);
    }

    @Test
    void getOneLoadsOwnedClaimAndClaimInvoices() {
        FakeRepository repository = new FakeRepository();
        repository.detail = new ClaimDetailEntity();
        repository.detail.setId("clm_1");
        repository.claimInvoices = List.of(invoice("inv_1", "10.50"));
        ClaimRecordUseCase useCase = new ClaimRecordUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);

        ClaimDetailEntity result = useCase.getOne("tenant_1", "user_1", "clm_1");

        assertThat(repository.detailClaimId).isEqualTo("clm_1");
        assertThat(repository.claimInvoicesTenantId).isEqualTo("tenant_1");
        assertThat(result.getInvoices()).hasSize(1);
        assertThat(result.getInvoiceIds()).containsExactly("inv_1");
    }

    @Test
    void reviewRecordsLoadsOwnedClaimBeforeQueryingRecords() {
        FakeRepository repository = new FakeRepository();
        repository.detail = new ClaimDetailEntity();
        repository.detail.setId("clm_1");
        ClaimReviewRecordEntity record = new ClaimReviewRecordEntity();
        record.setId("rr_1");
        repository.reviewRecords = List.of(record);
        ClaimRecordUseCase useCase = new ClaimRecordUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);

        List<ClaimReviewRecordEntity> result = useCase.reviewRecords("tenant_1", "user_1", "clm_1");

        assertThat(repository.detailClaimId).isEqualTo("clm_1");
        assertThat(repository.reviewRecordsTenantId).isEqualTo("tenant_1");
        assertThat(result).containsExactly(record);
    }

    private static ClaimRecordEntity invoice(String id, String amount) {
        ClaimRecordEntity entity = new ClaimRecordEntity();
        entity.setId(id);
        entity.setTotalAmount(new BigDecimal(amount));
        entity.setCurrency("CNY");
        return entity;
    }

    private static final class FakeNonceRepository implements ClaimNonceRepository {
        private String operation;
        private String resourceId;

        @Override
        public void verifyAndConsume(String tenantId, String ownerUserId, String sessionId, String operation, String nonce) {
            this.operation = operation;
        }

        @Override
        public void verifyAndConsume(
                String tenantId,
                String ownerUserId,
                String sessionId,
                String operation,
                String resourceId,
                String nonce
        ) {
            this.operation = operation;
            this.resourceId = resourceId;
        }
    }

    private static final class FakeRepository implements ClaimRecordRepository {
        private String tenantId;
        private String userId;
        private int page;
        private int pageSize;
        private List<ClaimDetailEntity> claims = List.of();
        private ClaimDetailEntity detail;
        private String detailClaimId;
        private List<ClaimRecordEntity> claimInvoices = List.of();
        private String claimInvoicesTenantId;
        private List<ClaimReviewRecordEntity> reviewRecords = List.of();
        private String reviewRecordsTenantId;
        private List<ClaimRecordEntity> claimableInvoices = List.of();
        private ClaimSubmitEntity existingClaim;
        private ClaimSubmitEntity savedClaim;
        private ClaimSubmitEntity updatedClaim;
        private ClaimSubmitEntity updatedDraftClaim;
        private String deletedClaimItemsClaimId;
        private List<ClaimItemEntity> savedItems = new ArrayList<>();

        @Override
        public PageResponse<ClaimRecordEntity> listClaimable(String tenantId, String userId, int page, int pageSize) {
            this.tenantId = tenantId;
            this.userId = userId;
            this.page = page;
            this.pageSize = pageSize;
            return PageResponse.success(List.of(), page, pageSize, 0);
        }

        @Override
        public PageResponse<ClaimDetailEntity> listClaims(String tenantId, String userId, int page, int pageSize) {
            return PageResponse.success(claims, page, pageSize, claims.size());
        }

        @Override
        public ClaimDetailEntity findDetail(String tenantId, String userId, String claimId) {
            this.detailClaimId = claimId;
            return detail;
        }

        @Override
        public List<ClaimRecordEntity> findClaimableInvoices(String tenantId, String userId, List<String> invoiceIds) {
            return claimableInvoices;
        }

        @Override
        public List<ClaimRecordEntity> findClaimInvoices(String tenantId, String claimId) {
            this.claimInvoicesTenantId = tenantId;
            return claimInvoices;
        }

        @Override
        public List<ClaimReviewRecordEntity> findReviewRecords(String tenantId, String claimId) {
            this.reviewRecordsTenantId = tenantId;
            return reviewRecords;
        }

        @Override
        public ClaimSubmitEntity findClaim(String tenantId, String userId, String claimId) {
            return existingClaim;
        }

        @Override
        public void saveClaim(ClaimSubmitEntity claim) {
            this.savedClaim = claim;
        }

        @Override
        public void updateClaim(ClaimSubmitEntity claim) {
            this.updatedClaim = claim;
        }

        @Override
        public void updateDraftClaim(ClaimSubmitEntity claim) {
            this.updatedDraftClaim = claim;
        }

        @Override
        public void saveClaimItem(ClaimItemEntity item) {
            savedItems.add(item);
        }

        @Override
        public void deleteClaimItems(
                String tenantId,
                String claimId,
                OffsetDateTime updatedAt,
                String updatedBy,
                String updatedTrace
        ) {
            this.deletedClaimItemsClaimId = claimId;
        }
    }
}
