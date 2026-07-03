package cloud.techotakus.invoice.integration.domain.entity;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class OcrVatVerificationResponseEntity {
    private String providerCode;
    private String providerRequestId;
    private String status;
    private Boolean verified;
    private Boolean matched;
    private Boolean confidenceBoosted;
    private Double confidenceDelta;
    private String errorCode;
    private String errorMessage;
    private Map<String, Object> verifiedInvoice = new LinkedHashMap<>();
    private Map<String, Object> rawResult = new LinkedHashMap<>();
}
