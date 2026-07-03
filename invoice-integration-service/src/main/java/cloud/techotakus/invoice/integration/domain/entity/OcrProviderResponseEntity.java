package cloud.techotakus.invoice.integration.domain.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class OcrProviderResponseEntity {
    private boolean success;
    private String providerCode;
    private String providerRequestId;
    private Boolean retryable;
    private String errorCode;
    private String errorMessage;
    private Integer totalPdfCount;
    private List<OcrInvoiceItem> invoiceItems = new ArrayList<>();
    private OcrVatVerificationResponseEntity vatVerification;
    private Map<String, Object> rawResult = new LinkedHashMap<>();
}
