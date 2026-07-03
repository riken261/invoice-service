package cloud.techotakus.invoice.core.domain.entity;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
public class InvoiceRecognizeSessionEntity {
    private String id;
    private String batchId;
    private String tenantId;
    private String nonce;
    private String sessionId;
    private String invoiceFileId;
    private String ownerUserId;
    private String idempotencyKey;
    private String source;
    private String provider;
    private String status;
    private String rawRequestId;
    private Map<String, Object> rawResult;
    private Map<String, Object> normalizedResult;
    private String errorCode;
    private String errorMessage;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
    private OffsetDateTime expiresAt;
    private String submittedInvoiceId;
    private OffsetDateTime createdAt;
    private String createdBy;
    private String createdTrace;
    private OffsetDateTime updatedAt;
    private String updatedBy;
    private String updatedTrace;
    private Boolean deleted;
    private List<InvoiceRecognizeUploadFileEntity> files;
}
