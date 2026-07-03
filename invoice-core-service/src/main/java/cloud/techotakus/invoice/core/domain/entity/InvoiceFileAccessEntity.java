package cloud.techotakus.invoice.core.domain.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class InvoiceFileAccessEntity {
    private String invoiceId;
    private String fileId;
    private String url;
    private String method;
    private Integer expiresInSeconds;
    private OffsetDateTime expiresAt;
    private String filename;
    private String contentType;
    private String accessType;
}
