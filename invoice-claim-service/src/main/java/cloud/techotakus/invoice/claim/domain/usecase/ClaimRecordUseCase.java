package cloud.techotakus.invoice.claim.domain.usecase;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.claim.domain.entity.ClaimDetailEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimItemEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimRecordEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimReviewRecordEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimSubmitEntity;
import cloud.techotakus.invoice.claim.domain.repository.ClaimNonceRepository;
import cloud.techotakus.invoice.claim.domain.repository.ClaimRecordRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ClaimRecordUseCase {

    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String DEFAULT_CURRENCY = "CNY";
    private static final String SYSTEM_ACTOR = "CLAIM_SERVICE";

    @Resource
    private ClaimRecordRepository repository;

    @Resource
    private ClaimNonceRepository nonceRepository;

    @Transactional(readOnly = true)
    public PageResponse<ClaimRecordEntity> list(String tenantId, String userId, int page, int pageSize) {
        return repository.listClaimable(
                required(tenantId, "tenantId"),
                required(userId, "userId"),
                normalizePage(page),
                normalizePageSize(pageSize)
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<ClaimDetailEntity> claims(String tenantId, String userId, int page, int pageSize) {
        String normalizedTenantId = required(tenantId, "tenantId");
        PageResponse<ClaimDetailEntity> result = repository.listClaims(
                normalizedTenantId,
                required(userId, "userId"),
                normalizePage(page),
                normalizePageSize(pageSize)
        );
        result.getItems().forEach(claim -> hydrateClaimInvoices(normalizedTenantId, claim));
        return result;
    }

    @Transactional(readOnly = true)
    public ClaimDetailEntity getOne(String tenantId, String userId, String claimId) {
        String normalizedTenantId = required(tenantId, "tenantId");
        ClaimDetailEntity claim = loadOwnedClaim(normalizedTenantId, userId, claimId);
        hydrateClaimInvoices(normalizedTenantId, claim);
        return claim;
    }

    @Transactional(readOnly = true)
    public List<ClaimReviewRecordEntity> reviewRecords(String tenantId, String userId, String claimId) {
        String normalizedTenantId = required(tenantId, "tenantId");
        ClaimDetailEntity claim = loadOwnedClaim(normalizedTenantId, userId, claimId);
        return repository.findReviewRecords(normalizedTenantId, claim.getId());
    }

    @Transactional
    public ClaimSubmitEntity save(ClaimSubmitEntity request) {
        validateSave(request);
        String tenantId = required(request.getTenantId(), "tenantId");
        String userId = required(request.getApplicantUserId(), "applicantUserId");
        String sessionId = required(request.getSessionId(), "sessionId");
        nonceRepository.verifyAndConsume(tenantId, userId, sessionId, "claim.save", required(request.getNonce(), "nonce"));

        List<String> invoiceIds = normalizeInvoiceIds(request.getInvoiceIds());
        List<ClaimRecordEntity> invoices = repository.findClaimableInvoices(tenantId, userId, invoiceIds);
        if (invoices.size() != invoiceIds.size()) {
            throw new ServiceException("Some invoices are not claimable", ErrorCode.INVALID_STATE);
        }
        Map<String, ClaimRecordEntity> invoicesById = invoices.stream()
                .collect(Collectors.toMap(ClaimRecordEntity::getId, Function.identity()));

        OffsetDateTime now = OffsetDateTime.now();
        ClaimSubmitEntity claim = new ClaimSubmitEntity();
        claim.setId("clm_" + compactUuid());
        claim.setTenantId(tenantId);
        claim.setApplicantUserId(userId);
        claim.setClaimNo("CLM" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(now) + compactUuid().substring(0, 6));
        claim.setDescription(defaultText(request.getDescription(), request.getTitle()));
        claim.setStatus(STATUS_DRAFT);
        claim.setTotalAmount(invoices.stream()
                .map(ClaimRecordEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        claim.setCurrency(resolveCurrency(invoices));
        claim.setCreatedAt(now);
        claim.setUpdatedAt(now);
        claim.setCreatedBy(userId);
        claim.setUpdatedBy(SYSTEM_ACTOR);
        claim.setInvoiceIds(invoiceIds);
        claim.setIdempotencyKey(defaultText(request.getIdempotencyKey(), request.getNonce()));
        claim.setCreatedTrace(claim.getIdempotencyKey());
        claim.setUpdatedTrace(claim.getIdempotencyKey());
        claim.setDeleted(false);
        repository.saveClaim(claim);

        for (String invoiceId : invoiceIds) {
            ClaimRecordEntity invoice = invoicesById.get(invoiceId);
            ClaimItemEntity item = new ClaimItemEntity();
            item.setId("cli_" + compactUuid());
            item.setTenantId(tenantId);
            item.setClaimId(claim.getId());
            item.setInvoiceId(invoiceId);
            item.setExpenseCategory(normalizeCategory(request.getExpenseCategory()));
            item.setAmount(invoice.getTotalAmount());
            item.setDescription(claim.getDescription());
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            item.setCreatedBy(userId);
            item.setUpdatedBy(SYSTEM_ACTOR);
            item.setCreatedTrace(claim.getIdempotencyKey());
            item.setUpdatedTrace(claim.getIdempotencyKey());
            item.setDeleted(false);
            repository.saveClaimItem(item);
        }
        return claim;
    }

    @Transactional
    public ClaimSubmitEntity updateDraft(ClaimSubmitEntity request) {
        validateSave(request);
        String tenantId = required(request.getTenantId(), "tenantId");
        String userId = required(request.getApplicantUserId(), "applicantUserId");
        String claimId = required(request.getId(), "claimId");
        nonceRepository.verifyAndConsume(
                tenantId,
                userId,
                required(request.getSessionId(), "sessionId"),
                "claim.save",
                claimId,
                required(request.getNonce(), "nonce")
        );

        ClaimSubmitEntity claim = repository.findClaim(tenantId, userId, claimId);
        if (claim == null) {
            throw new ServiceException("Claim not found", ErrorCode.RESOURCE_NOT_FOUND);
        }
        if (!List.of(STATUS_DRAFT, STATUS_REJECTED).contains(claim.getStatus())) {
            throw new ServiceException("Only draft or rejected claim can be updated", ErrorCode.INVALID_STATE);
        }

        List<String> invoiceIds = normalizeInvoiceIds(request.getInvoiceIds());
        String trace = defaultText(request.getIdempotencyKey(), request.getNonce());
        OffsetDateTime now = OffsetDateTime.now();
        repository.deleteClaimItems(tenantId, claimId, now, userId, trace);

        List<ClaimRecordEntity> invoices = repository.findClaimableInvoices(tenantId, userId, invoiceIds);
        if (invoices.size() != invoiceIds.size()) {
            throw new ServiceException("Some invoices are not claimable", ErrorCode.INVALID_STATE);
        }
        Map<String, ClaimRecordEntity> invoicesById = invoices.stream()
                .collect(Collectors.toMap(ClaimRecordEntity::getId, Function.identity()));

        claim.setDescription(defaultText(request.getDescription(), request.getTitle()));
        claim.setStatus(STATUS_DRAFT);
        claim.setTotalAmount(invoices.stream()
                .map(ClaimRecordEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        claim.setCurrency(resolveCurrency(invoices));
        claim.setSubmittedAt(null);
        claim.setUpdatedAt(now);
        claim.setUpdatedBy(userId);
        claim.setUpdatedTrace(trace);
        claim.setInvoiceIds(invoiceIds);
        repository.updateDraftClaim(claim);

        for (String invoiceId : invoiceIds) {
            ClaimRecordEntity invoice = invoicesById.get(invoiceId);
            ClaimItemEntity item = new ClaimItemEntity();
            item.setId("cli_" + compactUuid());
            item.setTenantId(tenantId);
            item.setClaimId(claim.getId());
            item.setInvoiceId(invoiceId);
            item.setExpenseCategory(normalizeCategory(request.getExpenseCategory()));
            item.setAmount(invoice.getTotalAmount());
            item.setDescription(claim.getDescription());
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            item.setCreatedBy(userId);
            item.setUpdatedBy(userId);
            item.setCreatedTrace(trace);
            item.setUpdatedTrace(trace);
            item.setDeleted(false);
            repository.saveClaimItem(item);
        }
        return claim;
    }

    private void hydrateClaimInvoices(String tenantId, ClaimDetailEntity claim) {
        List<ClaimRecordEntity> invoices = repository.findClaimInvoices(tenantId, claim.getId());
        claim.setInvoices(invoices);
        claim.setInvoiceIds(invoices.stream().map(ClaimRecordEntity::getId).toList());
        claim.setExpenseCategory(invoices.stream()
                .map(ClaimRecordEntity::getExpenseCategory)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null));
    }

    @Transactional
    public ClaimSubmitEntity submit(ClaimSubmitEntity request) {
        validateSubmit(request);
        String tenantId = required(request.getTenantId(), "tenantId");
        String userId = required(request.getApplicantUserId(), "applicantUserId");
        String claimId = required(request.getId(), "claimId");
        nonceRepository.verifyAndConsume(
                tenantId,
                userId,
                required(request.getSessionId(), "sessionId"),
                "claim.submit",
                claimId,
                required(request.getNonce(), "nonce")
        );

        ClaimSubmitEntity claim = repository.findClaim(tenantId, userId, claimId);
        if (claim == null) {
            throw new ServiceException("Claim not found", ErrorCode.RESOURCE_NOT_FOUND);
        }
        if (!STATUS_DRAFT.equals(claim.getStatus())) {
            throw new ServiceException("Only draft claim can be submitted", ErrorCode.INVALID_STATE);
        }
        OffsetDateTime now = OffsetDateTime.now();
        claim.setStatus(STATUS_SUBMITTED);
        claim.setSubmittedAt(now);
        claim.setUpdatedAt(now);
        claim.setUpdatedBy(userId);
        claim.setUpdatedTrace(defaultText(request.getIdempotencyKey(), request.getNonce()));
        repository.updateClaim(claim);
        return claim;
    }

    private static void validateSave(ClaimSubmitEntity request) {
        if (request == null) {
            throw new ServiceException("Claim save request is required", ErrorCode.VALIDATION_ERROR);
        }
        required(request.getTenantId(), "tenantId");
        required(request.getApplicantUserId(), "applicantUserId");
        required(request.getSessionId(), "sessionId");
        required(request.getNonce(), "nonce");
        normalizeCategory(request.getExpenseCategory());
        normalizeInvoiceIds(request.getInvoiceIds());
    }

    private ClaimDetailEntity loadOwnedClaim(String tenantId, String userId, String claimId) {
        ClaimDetailEntity claim = repository.findDetail(
                required(tenantId, "tenantId"),
                required(userId, "userId"),
                required(claimId, "claimId")
        );
        if (claim == null) {
            throw new ServiceException("Claim not found", ErrorCode.RESOURCE_NOT_FOUND);
        }
        return claim;
    }

    private static void validateSubmit(ClaimSubmitEntity request) {
        if (request == null) {
            throw new ServiceException("Claim submit request is required", ErrorCode.VALIDATION_ERROR);
        }
        required(request.getTenantId(), "tenantId");
        required(request.getApplicantUserId(), "applicantUserId");
        required(request.getSessionId(), "sessionId");
        required(request.getId(), "claimId");
        required(request.getNonce(), "nonce");
    }

    private static List<String> normalizeInvoiceIds(List<String> invoiceIds) {
        if (invoiceIds == null || invoiceIds.isEmpty()) {
            throw new ServiceException("invoiceIds is required", ErrorCode.VALIDATION_ERROR);
        }
        List<String> normalized = invoiceIds.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
        if (normalized.isEmpty()) {
            throw new ServiceException("invoiceIds is required", ErrorCode.VALIDATION_ERROR);
        }
        if (new LinkedHashSet<>(normalized).size() != normalized.size()) {
            throw new ServiceException("invoiceIds contains duplicate values", ErrorCode.VALIDATION_ERROR);
        }
        return normalized;
    }

    private static String normalizeCategory(String value) {
        String normalized = required(value, "expenseCategory").toLowerCase();
        if (!List.of("travel", "office", "meal", "service").contains(normalized)) {
            throw new ServiceException("expenseCategory is invalid", ErrorCode.VALIDATION_ERROR);
        }
        return normalized;
    }

    private static String resolveCurrency(List<ClaimRecordEntity> invoices) {
        return invoices.stream()
                .map(ClaimRecordEntity::getCurrency)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(DEFAULT_CURRENCY);
    }

    private static String defaultText(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        return StringUtils.hasText(second) ? second.trim() : null;
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

    private static String compactUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
