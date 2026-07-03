package cloud.techotakus.invoice.integration.domain.entity;

import lombok.Data;

@Data
public class OcrProviderRequestEntity {
    private String traceId;
    private String tenantId;
    private String invoiceId;
    private String taskId;
    private String idempotencyKey;
    private String providerCode;
    private String fileId;
    private String fileName;
    private String fileMimeType;
    private String objectKey;
    private String fileBase64;
    private String fileUrl;
    private String invoiceTypeHint;
    private Boolean enablePdf;
    private Integer pdfPageNumber;
    private Boolean enableMultiplePage;
    private Boolean enableCutImage;
    private Boolean enableItemPolygon;
    private Boolean enableQrCode;
    private Boolean enableSeal;
}
