package cloud.techotakus.invoice.core.domain.entity;

import lombok.Data;

import java.util.Map;

@Data
public class InvoiceConfirmEntity {
    private String tenantId;
    private String ownerUserId;
    private String sessionId;
    private String recognitionSessionId;
    private String nonce;
    private Map<String, Object> confirmedFields;
}
