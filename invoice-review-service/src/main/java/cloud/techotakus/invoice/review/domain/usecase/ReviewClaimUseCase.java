package cloud.techotakus.invoice.review.domain.usecase;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimDecisionEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimDetailEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimInvoiceEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimPendingEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimReviewRecordEntity;
import cloud.techotakus.invoice.review.domain.repository.ReviewClaimRepository;
import cloud.techotakus.invoice.review.domain.repository.ReviewNonceRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ReviewClaimUseCase {

    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String ACTION_REJECT = "REJECT";
    private static final String ACTION_APPROVE = "APPROVE";
    private static final String RESOURCE_TYPE_CLAIM = "EXPENSE_CLAIM";
    private static final String OPERATION_CLAIM_REVIEW = "claim.review";

    @Resource
    private ReviewClaimRepository repository;

    @Resource
    private ReviewNonceRepository nonceRepository;

    @Transactional(readOnly = true)
    public PageResponse<ReviewClaimPendingEntity> pending(String tenantId, int page, int pageSize) {
        return repository.pending(required(tenantId, "tenantId"), normalizePage(page), normalizePageSize(pageSize));
    }

    @Transactional(readOnly = true)
    public ReviewClaimDetailEntity getOne(String tenantId, String claimId) {
        String normalizedTenantId = required(tenantId, "tenantId");
        ReviewClaimDetailEntity claim = loadClaim(normalizedTenantId, claimId);
        hydrateClaimInvoices(normalizedTenantId, claim);
        return claim;
    }

    @Transactional(readOnly = true)
    public List<ReviewClaimReviewRecordEntity> reviewRecords(String tenantId, String claimId) {
        String normalizedTenantId = required(tenantId, "tenantId");
        ReviewClaimDetailEntity claim = loadClaim(normalizedTenantId, claimId);
        return repository.findReviewRecords(normalizedTenantId, claim.getId());
    }

    @Transactional
    public ReviewClaimDecisionEntity reject(ReviewClaimDecisionEntity request) {
        return decide(request, STATUS_REJECTED, ACTION_REJECT);
    }

    @Transactional
    public ReviewClaimDecisionEntity approve(ReviewClaimDecisionEntity request) {
        return decide(request, STATUS_APPROVED, ACTION_APPROVE);
    }

    private ReviewClaimDecisionEntity decide(ReviewClaimDecisionEntity request, String afterStatus, String action) {
        validateDecision(request);
        String tenantId = required(request.getTenantId(), "tenantId");
        String reviewerId = required(request.getReviewerUserId(), "reviewerUserId");
        String claimId = required(request.getClaimId(), "claimId");
        nonceRepository.verifyAndConsume(
                tenantId,
                reviewerId,
                required(request.getSessionId(), "sessionId"),
                OPERATION_CLAIM_REVIEW,
                RESOURCE_TYPE_CLAIM,
                claimId,
                required(request.getNonce(), "nonce")
        );

        ReviewClaimDetailEntity claim = loadClaim(tenantId, claimId);
        String beforeStatus = claim.getStatus();
        OffsetDateTime now = OffsetDateTime.now();
        String trace = StringUtils.hasText(request.getIdempotencyKey())
                ? request.getIdempotencyKey().trim()
                : request.getNonce().trim();

        claim.setStatus(afterStatus);
        claim.setLatestReviewOpinion(trimToNull(request.getComment()));
        if (STATUS_APPROVED.equals(afterStatus)) {
            claim.setApprovedAt(now);
        }
        claim.setUpdatedAt(now);
        claim.setUpdatedBy(reviewerId);
        claim.setUpdatedTrace(trace);
        repository.updateClaim(claim);

        ReviewClaimReviewRecordEntity record = new ReviewClaimReviewRecordEntity();
        record.setId("rr_" + compactUuid());
        record.setTenantId(tenantId);
        record.setResourceType(RESOURCE_TYPE_CLAIM);
        record.setResourceId(claimId);
        record.setReviewerUserId(reviewerId);
        record.setAction(action);
        record.setOpinion(trimToNull(request.getComment()));
        record.setBeforeStatus(beforeStatus);
        record.setAfterStatus(afterStatus);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setCreatedBy(reviewerId);
        record.setUpdatedBy(reviewerId);
        record.setCreatedTrace(trace);
        record.setUpdatedTrace(trace);
        record.setDeleted(false);
        repository.saveReviewRecord(record);

        ReviewClaimDecisionEntity result = new ReviewClaimDecisionEntity();
        result.setClaimId(claimId);
        result.setStatus(afterStatus);
        result.setReviewId(record.getId());
        result.setAction(action);
        result.setComment(record.getOpinion());
        result.setReviewedAt(now);
        return result;
    }

    private ReviewClaimDetailEntity loadClaim(String tenantId, String claimId) {
        ReviewClaimDetailEntity claim = repository.findDetail(tenantId, required(claimId, "claimId"));
        if (claim == null) {
            throw new ServiceException("Claim not found", ErrorCode.RESOURCE_NOT_FOUND);
        }
        return claim;
    }

    private void hydrateClaimInvoices(String tenantId, ReviewClaimDetailEntity claim) {
        List<ReviewClaimInvoiceEntity> invoices = repository.findClaimInvoices(tenantId, claim.getId());
        claim.setInvoices(invoices);
        claim.setInvoiceIds(invoices.stream().map(ReviewClaimInvoiceEntity::getId).toList());
        claim.setExpenseCategory(invoices.stream()
                .map(ReviewClaimInvoiceEntity::getExpenseCategory)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null));
    }

    private static String required(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ServiceException(fieldName + " is required", ErrorCode.VALIDATION_ERROR);
        }
        return value.trim();
    }

    private static int normalizePage(int page) {
        return Math.max(page, 1);
    }

    private static int normalizePageSize(int pageSize) {
        return Math.clamp(pageSize, 1, 100);
    }

    private static void validateDecision(ReviewClaimDecisionEntity request) {
        if (request == null) {
            throw new ServiceException("Claim review request is required", ErrorCode.VALIDATION_ERROR);
        }
        required(request.getTenantId(), "tenantId");
        required(request.getReviewerUserId(), "reviewerUserId");
        required(request.getSessionId(), "sessionId");
        required(request.getClaimId(), "claimId");
        required(request.getNonce(), "nonce");
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static String compactUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
