package cloud.techotakus.invoice.core.domain.usecase;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.core.domain.entity.InvoiceFileAccessEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeSessionEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeUploadEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeUploadFileEntity;
import cloud.techotakus.invoice.core.domain.repository.InvoiceNonceRepository;
import cloud.techotakus.invoice.core.domain.repository.InvoiceRecognizeRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InvoiceRecognizeUseCase {

    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_OCR_SUCCESS = "OCR_SUCCESS";
    private static final String STATUS_LOW_CONFIDENCE = "LOW_CONFIDENCE";
    private static final String STATUS_OCR_FAILED = "OCR_FAILED";
    private static final String STATUS_EXPIRED = "EXPIRED";
    private static final String SYSTEM_ACTOR = "INVOICE_CORE_SERVICE";

    @Resource
    private InvoiceRecognizeRepository repository;

    @Resource
    private InvoiceNonceRepository nonceRepository;

    @Resource
    private TaskExecutor applicationTaskExecutor;

    @Value("${invoice.core.recognition.timeout-seconds:30}")
    private long timeoutSeconds;

    @Value("${invoice.core.recognition.next-poll-after-ms:1000}")
    private long nextPollAfterMs;

    @Value("${invoice.core.recognition.source:CAMERA}")
    private String source;

    public InvoiceRecognizeUploadEntity recognizeUpload(InvoiceRecognizeSessionEntity request) {
        validateUpload(request);
        nonceRepository.verifyAndConsume(
                request.getTenantId().trim(),
                request.getOwnerUserId().trim(),
                request.getSessionId().trim(),
                "invoice.recognize-upload",
                request.getNonce()
        );
        String batchId = "ocr_batch_" + compactUuid();
        List<InvoiceRecognizeSessionEntity> sessions = new ArrayList<>();
        for (InvoiceRecognizeUploadFileEntity file : request.getFiles()) {
            InvoiceRecognizeSessionEntity session = newSession(batchId, request, file);
            String fileId = repository.uploadFile(session, file);
            file.setFileId(fileId);
            file.setSessionId(session.getId());
            file.setStatus(STATUS_PROCESSING);
            session.setInvoiceFileId(fileId);
            session.setFiles(List.of(file));
            repository.saveSession(session);
            sessions.add(session);
            applicationTaskExecutor.execute(() -> processSession(session, file));
        }
        InvoiceRecognizeUploadEntity response = new InvoiceRecognizeUploadEntity();
        response.setBatchId(batchId);
        response.setSessions(sessions);
        return response;
    }

    public InvoiceRecognizeSessionEntity recognizeResult(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw new ServiceException("sessionId is required", ErrorCode.VALIDATION_ERROR);
        }
        InvoiceRecognizeSessionEntity session = repository.findSession(sessionId.trim());
        if (session == null) {
            throw new ServiceException("OCR session not found", ErrorCode.RESOURCE_NOT_FOUND);
        }
        if (STATUS_PROCESSING.equals(session.getStatus()) && OffsetDateTime.now().isAfter(session.getExpiresAt())) {
            session.setStatus(STATUS_EXPIRED);
            session.setErrorCode("OCR_SESSION_EXPIRED");
            session.setErrorMessage("OCR session expired");
            session.setFinishedAt(OffsetDateTime.now());
            session.setUpdatedAt(OffsetDateTime.now());
            session.setUpdatedBy(SYSTEM_ACTOR);
            repository.updateSession(session);
        }
        return session;
    }

    public List<InvoiceRecognizeSessionEntity> recognizeResults(String batchId) {
        if (!StringUtils.hasText(batchId)) {
            throw new ServiceException("batchId is required", ErrorCode.VALIDATION_ERROR);
        }
        List<InvoiceRecognizeSessionEntity> sessions = repository.findSessionsByBatchId(batchId.trim());
        if (sessions.isEmpty()) {
            throw new ServiceException("OCR batch not found", ErrorCode.RESOURCE_NOT_FOUND);
        }
        return sessions.stream()
                .map(session -> recognizeResult(session.getId()))
                .toList();
    }

    public InvoiceFileAccessEntity previewSession(String tenantId, String userId, String recognitionSessionId) {
        InvoiceRecognizeSessionEntity session = loadOwnedSession(tenantId, userId, recognitionSessionId);
        return repository.previewFile(required(session.getInvoiceFileId(), "invoiceFileId"));
    }

    public long nextPollAfterMs() {
        return nextPollAfterMs;
    }

    private InvoiceRecognizeSessionEntity loadOwnedSession(String tenantId, String userId, String recognitionSessionId) {
        InvoiceRecognizeSessionEntity session = repository.findSession(required(recognitionSessionId, "recognitionSessionId"));
        if (session == null) {
            throw new ServiceException("OCR session not found", ErrorCode.RESOURCE_NOT_FOUND);
        }
        if (!required(tenantId, "tenantId").equals(session.getTenantId())
                || !required(userId, "ownerUserId").equals(session.getOwnerUserId())) {
            throw new ServiceException("OCR session does not belong to current user", ErrorCode.PERMISSION_DENIED);
        }
        return session;
    }

    private void processSession(InvoiceRecognizeSessionEntity session, InvoiceRecognizeUploadFileEntity file) {
        try {
            if (OffsetDateTime.now().isAfter(session.getExpiresAt())) {
                expire(session);
                return;
            }
            session.setStartedAt(OffsetDateTime.now());
            session.setUpdatedAt(OffsetDateTime.now());
            session.setUpdatedBy(SYSTEM_ACTOR);
            repository.updateSession(session);

            InvoiceRecognizeSessionEntity recognized = repository.recognize(session, file);
            recognized.setFinishedAt(OffsetDateTime.now());
            recognized.setUpdatedAt(OffsetDateTime.now());
            recognized.setUpdatedBy(SYSTEM_ACTOR);
            recognized.setStatus(resolveSuccessStatus(recognized));
            repository.updateSession(recognized);
        } catch (Exception ex) {
            session.setStatus(STATUS_OCR_FAILED);
            session.setErrorCode("OCR_RECOGNIZE_FAILED");
            session.setErrorMessage(ex.getMessage());
            session.setFinishedAt(OffsetDateTime.now());
            session.setUpdatedAt(OffsetDateTime.now());
            session.setUpdatedBy(SYSTEM_ACTOR);
            repository.updateSession(session);
        }
    }

    private void expire(InvoiceRecognizeSessionEntity session) {
        session.setStatus(STATUS_EXPIRED);
        session.setErrorCode("OCR_SESSION_EXPIRED");
        session.setErrorMessage("OCR session expired");
        session.setFinishedAt(OffsetDateTime.now());
        session.setUpdatedAt(OffsetDateTime.now());
        session.setUpdatedBy(SYSTEM_ACTOR);
        repository.updateSession(session);
    }

    private String resolveSuccessStatus(InvoiceRecognizeSessionEntity session) {
        Map<String, Object> normalizedResult = session.getNormalizedResult();
        if (normalizedResult == null) {
            return STATUS_LOW_CONFIDENCE;
        }
        if (Boolean.FALSE.equals(normalizedResult.get("success"))) {
            return STATUS_OCR_FAILED;
        }
        Object invoiceItems = normalizedResult.get("invoiceItems");
        if (!(invoiceItems instanceof List<?> list) || list.isEmpty()) {
            return STATUS_LOW_CONFIDENCE;
        }
        if (list.stream().anyMatch(item -> item instanceof Map<?, ?> map && Boolean.TRUE.equals(map.get("lowConfidence")))) {
            return STATUS_LOW_CONFIDENCE;
        }
        return STATUS_OCR_SUCCESS;
    }

    private InvoiceRecognizeSessionEntity newSession(
            String batchId,
            InvoiceRecognizeSessionEntity request,
            InvoiceRecognizeUploadFileEntity file
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        InvoiceRecognizeSessionEntity session = new InvoiceRecognizeSessionEntity();
        session.setId("ocr_session_" + compactUuid());
        session.setBatchId(batchId);
        session.setTenantId(required(request.getTenantId(), "tenantId"));
        session.setOwnerUserId(required(request.getOwnerUserId(), "ownerUserId"));
        session.setIdempotencyKey(required(request.getNonce(), "nonce"));
        session.setSource(normalizeSource(source));
        session.setProvider("AUTO");
        session.setStatus(STATUS_PROCESSING);
        session.setExpiresAt(now.plusSeconds(timeoutSeconds));
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        session.setCreatedBy(request.getOwnerUserId());
        session.setUpdatedBy(SYSTEM_ACTOR);
        session.setDeleted(false);
        session.setFiles(List.of(file));
        return session;
    }

    private void validateUpload(InvoiceRecognizeSessionEntity request) {
        if (request == null) {
            throw new ServiceException("recognize upload request is required", ErrorCode.VALIDATION_ERROR);
        }
        required(request.getTenantId(), "tenantId");
        required(request.getOwnerUserId(), "ownerUserId");
        required(request.getSessionId(), "sessionId");
        required(request.getNonce(), "nonce");
        if (request.getFiles() == null || request.getFiles().isEmpty()) {
            throw new ServiceException("files is required", ErrorCode.VALIDATION_ERROR);
        }
        for (InvoiceRecognizeUploadFileEntity file : request.getFiles()) {
            required(file.getFileName(), "fileName");
            required(file.getMimeType(), "mimeType");
            required(file.getSha256(), "sha256");
            required(file.getObjectKey(), "objectKey");
            required(file.getContentBase64(), "contentBase64");
            if (file.getSize() == null || file.getSize() <= 0) {
                throw new ServiceException("size must be positive", ErrorCode.VALIDATION_ERROR);
            }
        }
    }

    private static String required(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ServiceException(fieldName + " is required", ErrorCode.VALIDATION_ERROR);
        }
        return value.trim();
    }

    private static String normalizeSource(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : "CAMERA";
    }

    private static String compactUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
