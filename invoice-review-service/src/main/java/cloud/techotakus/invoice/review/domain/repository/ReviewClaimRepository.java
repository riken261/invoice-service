package cloud.techotakus.invoice.review.domain.repository;

import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimDetailEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimInvoiceEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimPendingEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimReviewRecordEntity;

import java.util.List;

public interface ReviewClaimRepository {

    PageResponse<ReviewClaimPendingEntity> pending(String tenantId, int page, int pageSize);

    ReviewClaimDetailEntity findDetail(String tenantId, String claimId);

    List<ReviewClaimInvoiceEntity> findClaimInvoices(String tenantId, String claimId);

    List<ReviewClaimReviewRecordEntity> findReviewRecords(String tenantId, String claimId);

    void updateClaim(ReviewClaimDetailEntity claim);

    void saveReviewRecord(ReviewClaimReviewRecordEntity record);
}
