package cloud.techotakus.invoice.integration.infra.provider.ocr;

import cloud.techotakus.invoice.integration.domain.entity.OcrProviderRequestEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrProviderResponseEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrVatVerificationRequestEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrVatVerificationResponseEntity;
import cloud.techotakus.invoice.integration.domain.repository.OcrIntegrationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "invoice.integration.ocr.tencent", name = "enabled", havingValue = "true")
public class OcrTencentProvider implements OcrIntegrationRepository {

    private static final Logger log = LoggerFactory.getLogger(OcrTencentProvider.class);

    private static final String PROVIDER_CODE = "TENCENT_CLOUD";
    private static final String RECOGNIZE_ACTION = "RecognizeGeneralInvoice";
    private static final String VAT_VERIFY_ACTION = "VatInvoiceVerifyNew";
    private static final String VERIFIED = "VERIFIED";
    private static final String FAILED = "FAILED";
    private static final double VERIFY_SUCCESS_CONFIDENCE_DELTA = 0.10d;
    private static final double VERIFY_FAILED_CONFIDENCE_DELTA = -0.30d;
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final TencentCloudSigner signer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${invoice.integration.ocr.tencent.endpoint:https://ocr.tencentcloudapi.com}")
    private String endpoint;

    @Value("${invoice.integration.ocr.tencent.host:ocr.tencentcloudapi.com}")
    private String host;

    @Value("${invoice.integration.ocr.tencent.service:ocr}")
    private String service;

    @Value("${invoice.integration.ocr.tencent.version:2018-11-19}")
    private String version;

    @Value("${invoice.integration.ocr.tencent.region:ap-beijing}")
    private String region;

    @Value("${invoice.integration.ocr.tencent.language:zh-CN}")
    private String language;

    @Value("${invoice.integration.ocr.tencent.secret-id:}")
    private String secretId;

    @Value("${invoice.integration.ocr.tencent.secret-key:}")
    private String secretKey;

    @Value("${invoice.integration.ocr.tencent.token:}")
    private String token;

    @Value("${invoice.integration.ocr.tencent.timeout:30s}")
    private Duration timeout;

    public OcrTencentProvider(TencentCloudSigner signer) {
        this.signer = signer;
    }

    @Override
    public String providerCode() {
        return PROVIDER_CODE;
    }

    @Override
    public OcrProviderResponseEntity recognize(OcrProviderRequestEntity request) {
        validateRecognizeRequest(request);
        TencentCallResult result = callTencent(RECOGNIZE_ACTION, recognizePayload(request));
        OcrProviderResponseEntity response = new OcrProviderResponseEntity();
        response.setProviderCode(providerCode());
        response.setRetryable(false);
        response.setRawResult(result.rawBody());
        if (!result.success()) {
            response.setSuccess(false);
            response.setRetryable(isRetryable(result.errorCode(), result.httpStatus()));
            response.setErrorCode(mapTencentError(result.errorCode(), result.httpStatus()));
            response.setErrorMessage(safeErrorMessage(result.errorMessage()));
            response.setProviderRequestId(result.requestId());
            return response;
        }

        JsonNode body = objectMapper.valueToTree(result.rawBody()).path("Response");
        response.setSuccess(true);
        response.setProviderRequestId(body.path("RequestId").asText(null));
        if (body.hasNonNull("TotalPDFCount")) {
            response.setTotalPdfCount(body.path("TotalPDFCount").asInt());
        }
        return response;
    }

    @Override
    public OcrVatVerificationResponseEntity verify(OcrVatVerificationRequestEntity request) {
        validateVerifyRequest(request);
        TencentCallResult result = callTencent(VAT_VERIFY_ACTION, verifyPayload(request));
        OcrVatVerificationResponseEntity response = new OcrVatVerificationResponseEntity();
        response.setProviderCode(providerCode());
        response.setRawResult(result.rawBody());
        if (!result.success()) {
            response.setStatus(FAILED);
            response.setVerified(false);
            response.setMatched(false);
            response.setConfidenceBoosted(false);
            response.setConfidenceDelta(VERIFY_FAILED_CONFIDENCE_DELTA);
            response.setErrorCode(mapTencentError(result.errorCode(), result.httpStatus()));
            response.setErrorMessage(safeErrorMessage(result.errorMessage()));
            response.setProviderRequestId(result.requestId());
            return response;
        }

        JsonNode body = objectMapper.valueToTree(result.rawBody()).path("Response");
        response.setStatus(VERIFIED);
        response.setVerified(true);
        response.setMatched(true);
        response.setConfidenceBoosted(true);
        response.setConfidenceDelta(VERIFY_SUCCESS_CONFIDENCE_DELTA);
        response.setProviderRequestId(body.path("RequestId").asText(null));
        JsonNode invoice = body.path("Invoice");
        if (!invoice.isMissingNode() && !invoice.isNull()) {
            response.setVerifiedInvoice(objectMapper.convertValue(invoice, MAP_TYPE));
        }
        return response;
    }

    private TencentCallResult callTencent(String action, Object body) {
        ensureCredentialConfigured();
        try {
            String payload = objectMapper.writeValueAsString(body);
            TencentCloudSigner.TencentOcrOptions options = new TencentCloudSigner.TencentOcrOptions(
                    endpoint,
                    host,
                    service,
                    version,
                    region,
                    language,
                    secretId,
                    secretKey,
                    token
            );
            TencentCloudSigner.SignedTencentRequest signed = signer.sign(action, payload, Instant.now(), options);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(timeout)
                    .header("Authorization", signed.authorization())
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("X-TC-Action", signed.action())
                    .header("X-TC-Version", signed.version())
                    .header("X-TC-Timestamp", Long.toString(signed.timestamp()))
                    .POST(HttpRequest.BodyPublishers.ofString(payload));
            addHeaderIfPresent(requestBuilder, "X-TC-Region", signed.region());
            addHeaderIfPresent(requestBuilder, "X-TC-Language", signed.language());
            addHeaderIfPresent(requestBuilder, "X-TC-Token", signed.token());

            HttpResponse<String> response = HttpClient.newBuilder()
                    .connectTimeout(timeout)
                    .build()
                    .send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            Map<String, Object> raw = parseRawBody(response.body());
            TencentError error = extractTencentError(raw);
            boolean success = response.statusCode() >= 200 && response.statusCode() < 300 && error == null;
            if (!success) {
                log.warn("Tencent OCR returned failure action={} httpStatus={} providerErrorCode={} providerRequestId={}",
                        action,
                        response.statusCode(),
                        error == null ? null : error.code(),
                        extractRequestId(raw)
                );
            }
            return new TencentCallResult(
                    success,
                    response.statusCode(),
                    error == null ? null : error.code(),
                    error == null ? null : error.message(),
                    extractRequestId(raw),
                    raw
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Tencent OCR call interrupted", ex);
        } catch (Exception ex) {
            log.warn("Tencent OCR call failed action={} endpoint={} region={} exception={}",
                    action,
                    endpoint,
                    region,
                    ex.getClass().getName(),
                    ex
            );
            throw new IllegalStateException("Tencent OCR call failed", ex);
        }
    }

    private Map<String, Object> recognizePayload(OcrProviderRequestEntity request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfText(payload, "ImageBase64", request.getFileBase64());
        putIfText(payload, "ImageUrl", request.getFileUrl());
        payload.put("EnablePdf", defaultTrue(request.getEnablePdf()));
        putIfPresent(payload, "PdfPageNumber", request.getPdfPageNumber());
        payload.put("EnableMultiplePage", defaultFalse(request.getEnableMultiplePage()));
        payload.put("EnableCutImage", defaultFalse(request.getEnableCutImage()));
        payload.put("EnableItemPolygon", defaultTrue(request.getEnableItemPolygon()));
        payload.put("EnableQRCode", defaultTrue(request.getEnableQrCode()));
        payload.put("EnableSeal", defaultTrue(request.getEnableSeal()));
        return payload;
    }

    private Map<String, Object> verifyPayload(OcrVatVerificationRequestEntity request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfText(payload, "InvoiceNo", request.getInvoiceNo());
        putIfText(payload, "InvoiceDate", request.getInvoiceDate());
        putIfText(payload, "InvoiceCode", request.getInvoiceCode());
        putIfText(payload, "InvoiceKind", request.getInvoiceKind());
        putIfText(payload, "CheckCode", request.getCheckCode());
        putIfText(payload, "Amount", request.getAmount());
        putIfText(payload, "RegionCode", request.getRegionCode());
        putIfText(payload, "SellerTaxCode", request.getSellerTaxCode());
        putIfPresent(payload, "EnableCommonElectronic", request.getEnableCommonElectronic());
        putIfPresent(payload, "EnableTodayInvoice", request.getEnableTodayInvoice());
        return payload;
    }

    private Map<String, Object> parseRawBody(String body) {
        try {
            if (!StringUtils.hasText(body)) {
                return new LinkedHashMap<>();
            }
            return objectMapper.readValue(body, MAP_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("Tencent OCR returned invalid JSON", ex);
        }
    }

    private TencentError extractTencentError(Map<String, Object> raw) {
        JsonNode node = objectMapper.valueToTree(raw);
        JsonNode error = node.path("Response").path("Error");
        if (error.isMissingNode() || error.isNull()) {
            return null;
        }
        return new TencentError(error.path("Code").asText(null), error.path("Message").asText(null));
    }

    private String extractRequestId(Map<String, Object> raw) {
        JsonNode node = objectMapper.valueToTree(raw);
        return node.path("Response").path("RequestId").asText(null);
    }

    private void ensureCredentialConfigured() {
        if (!StringUtils.hasText(secretId) || !StringUtils.hasText(secretKey)) {
            throw new IllegalStateException("Tencent OCR credential is not configured");
        }
    }

    private void validateRecognizeRequest(OcrProviderRequestEntity request) {
        if (request == null) {
            throw new IllegalArgumentException("OCR request is required");
        }
        if (!StringUtils.hasText(request.getFileBase64()) && !StringUtils.hasText(request.getFileUrl())) {
            throw new IllegalArgumentException("Either fileBase64 or fileUrl is required");
        }
    }

    private void validateVerifyRequest(OcrVatVerificationRequestEntity request) {
        if (request == null) {
            throw new IllegalArgumentException("VAT verification request is required");
        }
        if (!StringUtils.hasText(request.getInvoiceNo()) || !StringUtils.hasText(request.getInvoiceDate())) {
            throw new IllegalArgumentException("invoiceNo and invoiceDate are required");
        }
    }

    private static Boolean defaultTrue(Boolean value) {
        return value == null || value;
    }

    private static Boolean defaultFalse(Boolean value) {
        return value != null && value;
    }

    private static void addHeaderIfPresent(HttpRequest.Builder builder, String name, String value) {
        if (StringUtils.hasText(value)) {
            builder.header(name, value);
        }
    }

    private static void putIfText(Map<String, Object> target, String key, String value) {
        if (StringUtils.hasText(value)) {
            target.put(key, value);
        }
    }

    private static void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    private static boolean isRetryable(String providerErrorCode, int httpStatus) {
        if (httpStatus == 429 || httpStatus >= 500) {
            return true;
        }
        return providerErrorCode != null && (
                providerErrorCode.contains("InternalError")
                        || providerErrorCode.contains("UnKnowError")
                        || providerErrorCode.contains("OcrFailed")
                        || providerErrorCode.contains("DownLoadError")
        );
    }

    private static String mapTencentError(String providerErrorCode, int httpStatus) {
        if (httpStatus == 429) {
            return "RATE_LIMITED";
        }
        if (httpStatus >= 500) {
            return "EXTERNAL_SERVICE_UNAVAILABLE";
        }
        if (providerErrorCode == null) {
            return "OCR_PROVIDER_FAILED";
        }
        if (providerErrorCode.contains("TooLargeFileError")) {
            return "FILE_TOO_LARGE";
        }
        if (providerErrorCode.contains("InvalidParameter")) {
            return "VALIDATION_ERROR";
        }
        if (providerErrorCode.contains("UnOpenError")
                || providerErrorCode.contains("InArrears")
                || providerErrorCode.contains("ResourcePackageRunOut")
                || providerErrorCode.contains("ChargeStatusException")) {
            return "EXTERNAL_SERVICE_UNAVAILABLE";
        }
        return "OCR_PROVIDER_FAILED";
    }

    private static String safeErrorMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return "Tencent OCR provider call failed";
        }
        String normalized = fixMojibake(message);
        return normalized.length() > 500 ? normalized.substring(0, 500) : normalized;
    }

    private static String fixMojibake(String message) {
        if (!looksLikeUtf8DecodedAsLatin1(message)) {
            return message;
        }
        return new String(message.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    private static boolean looksLikeUtf8DecodedAsLatin1(String message) {
        return message.indexOf('å') >= 0
                || message.indexOf('æ') >= 0
                || message.indexOf('ç') >= 0
                || message.indexOf('è') >= 0
                || message.indexOf('é') >= 0;
    }

    private record TencentError(String code, String message) {
    }

    private record TencentCallResult(
            boolean success,
            int httpStatus,
            String errorCode,
            String errorMessage,
            String requestId,
            Map<String, Object> rawBody
    ) {
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }
}
