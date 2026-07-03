package cloud.techotakus.invoice.integration.infra.provider.ocr;

import cloud.techotakus.invoice.integration.domain.entity.OcrProviderRequestEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrProviderResponseEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrVatVerificationRequestEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrVatVerificationResponseEntity;
import cloud.techotakus.invoice.integration.domain.repository.OcrIntegrationRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "invoice.integration.ocr", name = "mock-enabled", havingValue = "true", matchIfMissing = true)
public class OcrStubProvider implements OcrIntegrationRepository {

    private static final String PROVIDER_CODE = "MOCK";
    private static final String VERIFIED = "VERIFIED";

    @Override
    public String providerCode() {
        return PROVIDER_CODE;
    }

    @Override
    public OcrProviderResponseEntity recognize(OcrProviderRequestEntity request) {
        OcrProviderResponseEntity response = new OcrProviderResponseEntity();
        response.setSuccess(true);
        response.setProviderCode(providerCode());
        response.setProviderRequestId("mock-ocr-" + System.currentTimeMillis());
        response.setRetryable(false);
        response.setTotalPdfCount(1);
        response.setRawResult(mockRecognizeRawResult(request, response.getProviderRequestId()));
        return response;
    }

    @Override
    public OcrVatVerificationResponseEntity verify(OcrVatVerificationRequestEntity request) {
        String invoiceNo = request == null ? null : request.getInvoiceNo();
        String invoiceDate = request == null ? null : request.getInvoiceDate();
        String amount = request == null ? null : request.getAmount();
        OcrVatVerificationResponseEntity response = new OcrVatVerificationResponseEntity();
        response.setProviderCode(providerCode());
        response.setProviderRequestId("mock-vat-" + System.currentTimeMillis());
        response.setStatus(VERIFIED);
        response.setVerified(true);
        response.setMatched(true);
        response.setConfidenceBoosted(true);
        response.setConfidenceDelta(0.10d);
        response.setVerifiedInvoice(new LinkedHashMap<>(Map.of(
                "InvoiceNo", nullToMock(invoiceNo, "00000001"),
                "InvoiceDate", nullToMock(invoiceDate, "2026-01-01"),
                "Total", nullToMock(amount, "0.00")
        )));
        response.setRawResult(new LinkedHashMap<>(Map.of(
                "provider", providerCode(),
                "mock", true,
                "requestId", response.getProviderRequestId(),
                "verifiedAt", OffsetDateTime.now().toString()
        )));
        return response;
    }

    private Map<String, Object> mockRecognizeRawResult(OcrProviderRequestEntity request, String requestId) {
        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("Title", "Mock VAT Invoice");
        invoice.put("Number", "00000001");
        invoice.put("Date", "2026-01-01");
        invoice.put("SellerName", "Mock Seller Ltd.");
        invoice.put("BuyerName", "Mock Buyer Ltd.");
        invoice.put("Total", "128.00");

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("Code", "OK");
        item.put("Type", 3);
        item.put("TypeDescription", "VAT invoice");
        item.put("SingleInvoiceInfos", Map.of("VatElectronicCommonInvoice", invoice));
        item.put("Page", 1);
        item.put("QRCode", "mock-qrcode");

        Map<String, Object> providerResponse = new LinkedHashMap<>();
        providerResponse.put("MixedInvoiceItems", List.of(item));
        providerResponse.put("TotalPDFCount", 1);
        providerResponse.put("RequestId", requestId);

        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("Response", providerResponse);
        raw.put("provider", providerCode());
        raw.put("mock", true);
        raw.put("traceId", request == null ? null : request.getTraceId());
        return raw;
    }

    private static String nullToMock(String value, String mockValue) {
        return value == null || value.isBlank() ? mockValue : value;
    }
}
