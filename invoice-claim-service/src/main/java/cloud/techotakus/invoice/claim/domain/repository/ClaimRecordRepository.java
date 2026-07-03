package cloud.techotakus.invoice.claim.domain.repository;

import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.claim.domain.entity.ClaimDetailEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimItemEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimRecordEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimReviewRecordEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimSubmitEntity;

import java.time.OffsetDateTime;
import java.util.List;

public interface ClaimRecordRepository {

    PageResponse<ClaimRecordEntity> listClaimable(String tenantId, String userId, int page, int pageSize);

    PageResponse<ClaimDetailEntity> listClaims(String tenantId, String userId, int page, int pageSize);

    ClaimDetailEntity findDetail(String tenantId, String userId, String claimId);

    List<ClaimRecordEntity> findClaimableInvoices(String tenantId, String userId, List<String> invoiceIds);

    List<ClaimRecordEntity> findClaimInvoices(String tenantId, String claimId);

    List<ClaimReviewRecordEntity> findReviewRecords(String tenantId, String claimId);

    ClaimSubmitEntity findClaim(String tenantId, String userId, String claimId);

    void saveClaim(ClaimSubmitEntity claim);

    void updateClaim(ClaimSubmitEntity claim);

    void updateDraftClaim(ClaimSubmitEntity claim);

    void saveClaimItem(ClaimItemEntity item);

    void deleteClaimItems(String tenantId, String claimId, OffsetDateTime updatedAt, String updatedBy, String updatedTrace);
}
