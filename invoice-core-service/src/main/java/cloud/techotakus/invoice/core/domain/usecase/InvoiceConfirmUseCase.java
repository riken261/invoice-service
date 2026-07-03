package cloud.techotakus.invoice.core.domain.usecase;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.core.domain.entity.InvoiceConfirmEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceManualInputSubmitEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeSessionEntity;
import cloud.techotakus.invoice.core.domain.repository.InvoiceNonceRepository;
import cloud.techotakus.invoice.core.domain.repository.InvoiceRecognizeRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InvoiceConfirmUseCase {

    private static final String SYSTEM_ACTOR = "INVOICE_CORE_SERVICE";
    private static final String STATUS_USER_CONFIRMED = "USER_CONFIRMED";
    private static final String DUPLICATE_NO = "NO_DUPLICATE";
    private static final String OCR_PROCESSING = "PROCESSING";
    private static final String OCR_SUCCESS = "OCR_SUCCESS";
    private static final String OCR_SUBMITTED = "SUBMITTED";
    private static final String OCR_EXPIRED = "EXPIRED";

    @Resource
    private InvoiceRecognizeRepository repository;

    @Resource
    private InvoiceNonceRepository nonceRepository;

    @Transactional
    public InvoiceEntity confirm(InvoiceConfirmEntity request) {
        validateConfirm(request);
        nonceRepository.verifyAndConsume(
                request.getTenantId().trim(),
                request.getOwnerUserId().trim(),
                request.getSessionId().trim(),
                "invoice.confirm",
                request.getRecognitionSessionId().trim(),
                request.getNonce()
        );

        InvoiceRecognizeSessionEntity session = loadConsumableSession(
                request.getRecognitionSessionId(),
                request.getTenantId(),
                request.getOwnerUserId()
        );
        if (StringUtils.hasText(session.getSubmittedInvoiceId())) {
            return loadInvoice(session.getSubmittedInvoiceId());
        }

        InvoiceEntity existing = repository.findInvoiceByFileId(session.getInvoiceFileId());
        if (existing != null) {
            markSessionSubmitted(session, existing.getId(), request.getOwnerUserId(), request.getNonce());
            return existing;
        }

        Map<String, Object> fields = request.getConfirmedFields() == null || request.getConfirmedFields().isEmpty()
                ? defaultConfirmedFields(session.getNormalizedResult())
                : normalizeFields(request.getConfirmedFields(), "confirmedFields");
        if (fields.isEmpty()) {
            throw new ServiceException("confirmedFields is required", ErrorCode.VALIDATION_ERROR);
        }

        InvoiceEntity invoice = newInvoice(session, request.getOwnerUserId(), fields, request.getNonce());
        invoice.setConfirmedFields(fields);
        repository.saveInvoice(invoice);
        markSessionSubmitted(session, invoice.getId(), request.getOwnerUserId(), request.getNonce());
        return invoice;
    }

    @Transactional
    public InvoiceEntity submitManualInput(InvoiceManualInputSubmitEntity request) {
        validateManualInput(request);
        String invoiceId = required(request.getInvoiceId(), "invoiceId");
        nonceRepository.verifyAndConsume(
                request.getTenantId().trim(),
                request.getOwnerUserId().trim(),
                request.getSessionId().trim(),
                "invoice.manual-input",
                invoiceId,
                request.getNonce()
        );

        InvoiceEntity invoice = repository.findInvoice(invoiceId);
        if (invoice == null) {
            throw new ServiceException("Invoice not found", ErrorCode.RESOURCE_NOT_FOUND);
        }
        String tenantId = request.getTenantId().trim();
        String ownerUserId = request.getOwnerUserId().trim();
        if (!tenantId.equals(invoice.getTenantId()) || !ownerUserId.equals(invoice.getOwnerUserId())) {
            throw new ServiceException("Invoice does not belong to current user", ErrorCode.PERMISSION_DENIED);
        }

        Map<String, Object> fields = normalizeFields(request.getFields(), "fields");
        invoice.setManualInput(true);
        invoice.setManualFields(fields);
        invoice.setConfirmedFields(fields);
        invoice.setInvoiceStatus(STATUS_USER_CONFIRMED);
        if (!StringUtils.hasText(invoice.getDuplicateStatus())) {
            invoice.setDuplicateStatus(DUPLICATE_NO);
        }
        invoice.setUpdatedAt(OffsetDateTime.now());
        invoice.setUpdatedBy(ownerUserId);
        invoice.setUpdatedTrace(StringUtils.hasText(request.getIdempotencyKey())
                ? request.getIdempotencyKey().trim()
                : request.getNonce());
        applyFields(invoice, fields);
        repository.updateInvoice(invoice);
        return invoice;
    }

    @Transactional
    public InvoiceEntity submitSessionManualInput(InvoiceManualInputSubmitEntity request) {
        validateSessionManualInput(request);
        String recognitionSessionId = required(request.getRecognitionSessionId(), "recognitionSessionId");
        nonceRepository.verifyAndConsume(
                request.getTenantId().trim(),
                request.getOwnerUserId().trim(),
                request.getSessionId().trim(),
                "invoice.manual-input",
                recognitionSessionId,
                request.getNonce()
        );

        InvoiceRecognizeSessionEntity session = loadConsumableSession(
                recognitionSessionId,
                request.getTenantId(),
                request.getOwnerUserId()
        );
        if (StringUtils.hasText(session.getSubmittedInvoiceId())) {
            return loadInvoice(session.getSubmittedInvoiceId());
        }

        InvoiceEntity existing = repository.findInvoiceByFileId(session.getInvoiceFileId());
        if (existing != null) {
            markSessionSubmitted(session, existing.getId(), request.getOwnerUserId(), request.getNonce());
            return existing;
        }

        Map<String, Object> fields = normalizeFields(request.getFields(), "fields");
        InvoiceEntity invoice = newInvoice(session, request.getOwnerUserId(), fields, request.getNonce());
        invoice.setManualInput(true);
        invoice.setManualFields(fields);
        invoice.setConfirmedFields(fields);
        invoice.setUpdatedTrace(StringUtils.hasText(request.getIdempotencyKey())
                ? request.getIdempotencyKey().trim()
                : request.getNonce());
        repository.saveInvoice(invoice);
        markSessionSubmitted(session, invoice.getId(), request.getOwnerUserId(), request.getNonce());
        return invoice;
    }

    private InvoiceRecognizeSessionEntity loadConsumableSession(String sessionId, String tenantId, String userId) {
        InvoiceRecognizeSessionEntity session = repository.findSession(required(sessionId, "recognitionSessionId"));
        if (session == null) {
            throw new ServiceException("OCR session not found", ErrorCode.RESOURCE_NOT_FOUND);
        }
        String normalizedTenantId = required(tenantId, "tenantId");
        String normalizedUserId = required(userId, "ownerUserId");
        if (!normalizedTenantId.equals(session.getTenantId()) || !normalizedUserId.equals(session.getOwnerUserId())) {
            throw new ServiceException("OCR session does not belong to current user", ErrorCode.PERMISSION_DENIED);
        }
        refreshExpired(session);
        if (OCR_EXPIRED.equals(session.getStatus())) {
            throw new ServiceException("OCR session is expired", ErrorCode.INVALID_STATE);
        }
        if (OCR_SUBMITTED.equals(session.getStatus()) && !StringUtils.hasText(session.getSubmittedInvoiceId())) {
            throw new ServiceException("OCR session has already been submitted", ErrorCode.DUPLICATE_SUBMIT);
        }
        return session;
    }

    private void refreshExpired(InvoiceRecognizeSessionEntity session) {
        if (session.getExpiresAt() != null
                && session.getExpiresAt().isBefore(OffsetDateTime.now())
                && OCR_PROCESSING.equals(session.getStatus())
                && !OCR_SUBMITTED.equals(session.getStatus())
                && !OCR_EXPIRED.equals(session.getStatus())) {
            session.setStatus(OCR_EXPIRED);
            session.setUpdatedAt(OffsetDateTime.now());
            session.setUpdatedBy(SYSTEM_ACTOR);
            repository.updateSession(session);
        }
    }

    private InvoiceEntity loadInvoice(String invoiceId) {
        InvoiceEntity invoice = repository.findInvoice(required(invoiceId, "invoiceId"));
        if (invoice == null) {
            throw new ServiceException("Invoice not found", ErrorCode.RESOURCE_NOT_FOUND);
        }
        return invoice;
    }

    private InvoiceEntity newInvoice(
            InvoiceRecognizeSessionEntity session,
            String userId,
            Map<String, Object> fields,
            String idempotencyKey
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setId("inv_" + UUID.randomUUID().toString().replace("-", ""));
        invoice.setTenantId(session.getTenantId());
        invoice.setInvoiceFileId(session.getInvoiceFileId());
        invoice.setOwnerUserId(session.getOwnerUserId());
        invoice.setInvoiceType(defaultText(value(fields, "invoiceType", "invoice_type", "type"), "GENERAL"));
        invoice.setCurrency(defaultText(value(fields, "currency"), "CNY"));
        invoice.setOcrStatus(OCR_SUCCESS);
        invoice.setInvoiceStatus(STATUS_USER_CONFIRMED);
        invoice.setDuplicateStatus(DUPLICATE_NO);
        invoice.setManualInput(false);
        invoice.setOcrFields(safeMap(session.getNormalizedResult()));
        invoice.setManualFields(new LinkedHashMap<>());
        invoice.setCreatedAt(now);
        invoice.setUpdatedAt(now);
        invoice.setCreatedBy(userId.trim());
        invoice.setUpdatedBy(SYSTEM_ACTOR);
        invoice.setCreatedTrace(idempotencyKey);
        invoice.setDeleted(false);
        applyFields(invoice, fields);
        return invoice;
    }

    private void markSessionSubmitted(
            InvoiceRecognizeSessionEntity session,
            String invoiceId,
            String userId,
            String idempotencyKey
    ) {
        session.setStatus(OCR_SUBMITTED);
        session.setSubmittedInvoiceId(invoiceId);
        session.setFinishedAt(OffsetDateTime.now());
        session.setUpdatedAt(OffsetDateTime.now());
        session.setUpdatedBy(userId.trim());
        session.setUpdatedTrace(idempotencyKey);
        repository.updateSession(session);
    }

    private void applyFields(InvoiceEntity invoice, Map<String, Object> fields) {
        setIfPresent(fields, invoice::setInvoiceType, "invoiceType", "invoice_type", "type");
        setIfPresent(fields, invoice::setInvoiceCode, "invoiceCode", "invoice_code", "code");
        setIfPresent(fields, invoice::setInvoiceNumber, "invoiceNo", "invoiceNumber", "invoice_number", "number");
        setDateIfPresent(fields, invoice::setIssueDate, "issueDate", "invoiceDate", "issue_date");
        setIfPresent(fields, invoice::setSellerName, "sellerName", "seller_name");
        setIfPresent(fields, invoice::setBuyerName, "buyerName", "buyer_name");
        setAmountIfPresent(fields, invoice::setAmountWithoutTax, "amountWithoutTax", "amount_without_tax");
        setAmountIfPresent(fields, invoice::setTaxAmount, "taxAmount", "tax_amount");
        setAmountIfPresent(fields, invoice::setTotalAmount, "totalAmount", "amount", "total_amount");
        setIfPresent(fields, invoice::setCurrency, "currency");
    }

    private static Map<String, Object> defaultConfirmedFields(Map<String, Object> normalizedResult) {
        Map<String, Object> normalized = safeMap(normalizedResult);
        Object invoiceItems = normalized.get("invoiceItems");
        if (invoiceItems instanceof List<?> list && !list.isEmpty() && list.getFirst() instanceof Map<?, ?> first) {
            Map<String, Object> fields = new LinkedHashMap<>();
            first.forEach((key, value) -> {
                if (key != null) {
                    fields.put(key.toString(), value);
                }
            });
            return fields;
        }
        return normalized;
    }

    private static Map<String, Object> normalizeFields(Map<String, Object> fields, String name) {
        if (fields == null || fields.isEmpty()) {
            throw new ServiceException(name + " is required", ErrorCode.VALIDATION_ERROR);
        }
        return new LinkedHashMap<>(fields);
    }

    private static Map<String, Object> safeMap(Map<String, Object> fields) {
        return fields == null ? new LinkedHashMap<>() : new LinkedHashMap<>(fields);
    }

    private static String required(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ServiceException(fieldName + " is required", ErrorCode.VALIDATION_ERROR);
        }
        return value.trim();
    }

    private static String value(Map<String, Object> fields, String... names) {
        for (String name : names) {
            Object value = fields.get(name);
            if (value != null && StringUtils.hasText(value.toString())) {
                return value.toString().trim();
            }
        }
        return null;
    }

    private static String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private static LocalDate dateValue(Map<String, Object> fields, String... names) {
        String value = value(fields, names);
        return StringUtils.hasText(value) ? LocalDate.parse(value) : null;
    }

    private static BigDecimal amountValue(Map<String, Object> fields, String... names) {
        String value = value(fields, names);
        return StringUtils.hasText(value) ? new BigDecimal(value) : null;
    }

    private static void setIfPresent(Map<String, Object> fields, java.util.function.Consumer<String> setter, String... names) {
        String value = value(fields, names);
        if (StringUtils.hasText(value)) {
            setter.accept(value);
        }
    }

    private static void setDateIfPresent(Map<String, Object> fields, java.util.function.Consumer<LocalDate> setter, String... names) {
        LocalDate value = dateValue(fields, names);
        if (value != null) {
            setter.accept(value);
        }
    }

    private static void setAmountIfPresent(Map<String, Object> fields, java.util.function.Consumer<BigDecimal> setter, String... names) {
        BigDecimal value = amountValue(fields, names);
        if (value != null) {
            setter.accept(value);
        }
    }

    private static void validateConfirm(InvoiceConfirmEntity request) {
        if (request == null) {
            throw new ServiceException("Invoice confirm request is required", ErrorCode.VALIDATION_ERROR);
        }
        required(request.getTenantId(), "tenantId");
        required(request.getOwnerUserId(), "ownerUserId");
        required(request.getSessionId(), "sessionId");
        required(request.getRecognitionSessionId(), "recognitionSessionId");
        required(request.getNonce(), "nonce");
    }

    private static void validateManualInput(InvoiceManualInputSubmitEntity request) {
        if (request == null) {
            throw new ServiceException("Invoice manual input request is required", ErrorCode.VALIDATION_ERROR);
        }
        required(request.getTenantId(), "tenantId");
        required(request.getOwnerUserId(), "ownerUserId");
        required(request.getSessionId(), "sessionId");
        required(request.getInvoiceId(), "invoiceId");
        required(request.getNonce(), "nonce");
        normalizeFields(request.getFields(), "fields");
    }

    private static void validateSessionManualInput(InvoiceManualInputSubmitEntity request) {
        if (request == null) {
            throw new ServiceException("Invoice session manual input request is required", ErrorCode.VALIDATION_ERROR);
        }
        required(request.getTenantId(), "tenantId");
        required(request.getOwnerUserId(), "ownerUserId");
        required(request.getSessionId(), "sessionId");
        required(request.getRecognitionSessionId(), "recognitionSessionId");
        required(request.getNonce(), "nonce");
        normalizeFields(request.getFields(), "fields");
    }
}
