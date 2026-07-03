package cloud.techotakus.invoice.ocr.domain.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OcrRecognizeResponseEntity {
    private boolean success;
    private String providerCode;
    private String providerRequestId;
    private Boolean retryable;
    private String errorCode;
    private String errorMessage;
    private Integer totalPdfCount;
    private List<Map<String, Object>> invoiceItems;
    private OcrVatVerificationResponseEntity vatVerification;
    private Map<String, Object> rawResult;
}
