package cloud.techotakus.invoice.core.domain.entity;

import lombok.Data;

@Data
public class InvoiceRecognizeUploadFileEntity {
    private String fileName;
    private String mimeType;
    private Long size;
    private String sha256;
    private String objectKey;
    private String contentBase64;
    private String invoiceTypeHint;
    private String fileId;
    private String sessionId;
    private String status;
}
