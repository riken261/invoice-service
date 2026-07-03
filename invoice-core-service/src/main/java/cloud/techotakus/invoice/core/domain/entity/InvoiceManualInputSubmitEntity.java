package cloud.techotakus.invoice.core.domain.entity;

import lombok.Data;

import java.util.Map;

@Data
public class InvoiceManualInputSubmitEntity {
    private String tenantId;
    private String ownerUserId;
    private String sessionId;
    private String invoiceId;
    private String recognitionSessionId;
    private String nonce;
    private String idempotencyKey;
    private Map<String, Object> fields;
}
