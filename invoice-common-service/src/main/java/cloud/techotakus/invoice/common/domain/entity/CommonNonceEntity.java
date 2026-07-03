package cloud.techotakus.invoice.common.domain.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CommonNonceEntity {
    private String tenantId;
    private String ownerUserId;
    private String sessionId;
    private String operation;
    private String resourceType;
    private String resourceId;
    private String nonce;
    private OffsetDateTime expiresAt;
}
