package cloud.techotakus.invoice.ocr.domain.usecase;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.ocr.domain.entity.OcrRecognizeRequestEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrRecognizeResponseEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrVatVerificationRequestEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrVatVerificationResponseEntity;
import cloud.techotakus.invoice.ocr.domain.repository.OcrOperationRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrOperationUseCase {

    private static final String VAT_INVOICE = "VAT_INVOICE";
    private static final double ZERO_CONFIDENCE = 0.0d;
    private static final double MAX_CONFIDENCE = 1.0d;
    private static final Pattern DATE_PARTS = Pattern.compile("^(\\d{4})\\D+(\\d{1,2})\\D+(\\d{1,2}).*$");
    private static final int FULLY_DIGITAL_INVOICE_NO_LENGTH = 20;

    @Resource
    private OcrOperationRepository repository;

    @Value("${invoice.ocr.vat-verify-enabled:true}")
    private boolean vatVerifyEnabled;

    @Value("${invoice.ocr.base-confidence:0.85}")
    private double baseConfidence;

    @Value("${invoice.ocr.low-confidence-threshold:0.70}")
    private double lowConfidenceThreshold;

    @Value("${invoice.ocr.vat-verify-failed-confidence-delta:-0.30}")
    private double vatVerifyFailedConfidenceDelta;

    public OcrRecognizeResponseEntity recognize(OcrRecognizeRequestEntity request) {
        validateRecognizeRequest(request);
        request.setProviderCode(null);
        OcrRecognizeResponseEntity response = normalize(repository.recognize(request));
        if (response == null || !response.isSuccess()) {
            return response;
        }
        maybeVerifyVatInvoice(request, response);
        return response;
    }

    public OcrVatVerificationResponseEntity verify(OcrVatVerificationRequestEntity request) {
        validateVerifyRequest(request);
        request.setProviderCode(null);
        return repository.verify(request);
    }

    private void maybeVerifyVatInvoice(OcrRecognizeRequestEntity sourceRequest, OcrRecognizeResponseEntity response) {
        if (!vatVerifyEnabled || response.getInvoiceItems() == null) {
            return;
        }
        response.getInvoiceItems().stream()
                .filter(this::requiresVatVerification)
                .forEach(item -> applyVatVerification(sourceRequest, response, item));
    }

    private void applyVatVerification(
            OcrRecognizeRequestEntity sourceRequest,
            OcrRecognizeResponseEntity response,
            Map<String, Object> item
    ) {
        OcrVatVerificationResponseEntity verification;
        try {
            verification = repository.verify(toVerificationRequest(sourceRequest, item));
        } catch (RuntimeException ex) {
            verification = failedVerification(ex);
        }
        response.setVatVerification(verification);
        if (verification != null) {
            double currentConfidence = number(item.get("confidence"), baseConfidence);
            boolean matched = Boolean.TRUE.equals(verification.getVerified())
                    && Boolean.TRUE.equals(verification.getMatched());
            double delta = verification.getConfidenceDelta() == null
                    ? (matched ? 0.0d : vatVerifyFailedConfidenceDelta)
                    : verification.getConfidenceDelta();
            if (!matched && delta >= 0.0d) {
                delta = vatVerifyFailedConfidenceDelta;
            }
            item.put("vatVerified", verification.getVerified());
            item.put("vatMatched", verification.getMatched());
            addConfidenceReason(item, matched ? "VAT_VERIFY_MATCHED" : "VAT_VERIFY_NOT_MATCHED");
            applyConfidence(item, currentConfidence + delta);
        }
    }

    private OcrVatVerificationResponseEntity failedVerification(RuntimeException ex) {
        OcrVatVerificationResponseEntity response = new OcrVatVerificationResponseEntity();
        response.setStatus("FAILED");
        response.setVerified(false);
        response.setMatched(false);
        response.setConfidenceBoosted(false);
        response.setConfidenceDelta(vatVerifyFailedConfidenceDelta);
        response.setErrorMessage(ex.getMessage());
        return response;
    }

    private OcrVatVerificationRequestEntity toVerificationRequest(
            OcrRecognizeRequestEntity sourceRequest,
            Map<String, Object> item
    ) {
        OcrVatVerificationRequestEntity request = new OcrVatVerificationRequestEntity();
        request.setTraceId(sourceRequest.getTraceId());
        request.setTenantId(sourceRequest.getTenantId());
        request.setInvoiceId(sourceRequest.getInvoiceId());
        request.setTaskId(sourceRequest.getTaskId());
        request.setProviderCode(null);
        request.setInvoiceNo(normalizeDigits(item.get("invoiceNo")));
        request.setInvoiceDate(normalizeVerifyDate(item.get("invoiceDate")));
        request.setInvoiceCode(normalizeInvoiceCode(item.get("invoiceCode")));
        request.setInvoiceKind(resolveInvoiceKind(item));
        request.setCheckCode(normalizeCheckCode(item.get("checkCode")));
        request.setAmount(selectVerifyAmount(item, request.getInvoiceKind()));
        request.setRegionCode(text(item.get("regionCode")));
        request.setSellerTaxCode(resolveSellerTaxCode(item));
        request.setEnableCommonElectronic(resolveEnableCommonElectronic(item));
        request.setEnableTodayInvoice(true);
        return request;
    }

    private OcrRecognizeResponseEntity normalize(OcrRecognizeResponseEntity response) {
        if (response == null || !response.isSuccess() || response.getRawResult() == null) {
            return response;
        }
        if (response.getInvoiceItems() != null && !response.getInvoiceItems().isEmpty()) {
            return response;
        }
        Map<String, Object> body = asMap(response.getRawResult().get("Response"));
        if (body == null) {
            return response;
        }
        if (response.getProviderRequestId() == null) {
            response.setProviderRequestId(text(body.get("RequestId")));
        }
        if (response.getTotalPdfCount() == null) {
            response.setTotalPdfCount(integer(body.get("TotalPDFCount")));
        }
        List<Map<String, Object>> vendorItems = asMapList(body.get("MixedInvoiceItems"));
        if (vendorItems.isEmpty()) {
            response.setInvoiceItems(new ArrayList<>(List.of(manualInputItem("NO_INVOICE_ITEM_RECOGNIZED"))));
            return response;
        }
        List<Map<String, Object>> items = vendorItems.stream()
                .map(item -> normalizeItem(response.getProviderCode(), item))
                .toList();
        response.setInvoiceItems(new ArrayList<>(items));
        return response;
    }

    private Map<String, Object> normalizeItem(String providerCode, Map<String, Object> vendorItem) {
        Map<String, Object> invoiceInfo = flattenInvoiceInfo(asMap(vendorItem.get("SingleInvoiceInfos")));
        Integer providerType = integer(vendorItem.get("Type"));
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("code", text(vendorItem.get("Code")));
        item.put("providerCode", providerCode);
        item.put("providerType", providerType);
        item.put("providerSubType", text(vendorItem.get("SubType")));
        item.put("providerTypeDescription", text(vendorItem.get("TypeDescription")));
        item.put("providerSubTypeDescription", text(vendorItem.get("SubTypeDescription")));
        item.put("invoiceType", mapInvoiceType(providerType));
        item.put("page", integer(vendorItem.get("Page")));
        item.put("angle", vendorItem.get("Angle"));
        item.put("title", firstText(invoiceInfo, "Title"));
        item.put("invoiceCode", firstText(invoiceInfo, "Code", "CodeConfirm"));
        item.put("invoiceNo", firstText(invoiceInfo, "Number", "NumberConfirm"));
        item.put("invoiceDate", firstText(invoiceInfo, "Date"));
        item.put("checkCode", firstText(invoiceInfo, "CheckCode"));
        item.put("buyerName", firstText(invoiceInfo, "Buyer", "BuyerName"));
        item.put("buyerTaxId", firstText(invoiceInfo, "BuyerTaxID", "BuyerTaxCode"));
        item.put("sellerName", firstText(invoiceInfo, "Seller", "SellerName"));
        item.put("sellerTaxId", firstText(invoiceInfo, "SellerTaxID", "SellerTaxCode"));
        item.put("amountWithoutTax", firstMoney(invoiceInfo, "PretaxAmount", "AmountWithoutTax"));
        item.put("taxAmount", firstMoney(invoiceInfo, "Tax", "TaxAmount"));
        item.put("totalAmount", firstMoney(invoiceInfo, "Total", "AmountWithTax"));
        item.put("currency", "CNY");
        item.put("qrCode", text(vendorItem.get("QRCode")));
        item.put("cutImageBase64", text(vendorItem.get("CutImage")));
        item.put("rawInvoiceInfo", invoiceInfo);
        applyInitialConfidence(item);
        return item;
    }

    private void applyInitialConfidence(Map<String, Object> item) {
        List<String> reasons = new ArrayList<>();
        if (!StringUtils.hasText(text(item.get("invoiceNo")))) {
            reasons.add("MISSING_INVOICE_NO");
        }
        if (!StringUtils.hasText(text(item.get("invoiceDate")))) {
            reasons.add("MISSING_INVOICE_DATE");
        }
        if (item.get("totalAmount") == null) {
            reasons.add("MISSING_TOTAL_AMOUNT");
        }
        if (!StringUtils.hasText(text(item.get("sellerName")))) {
            reasons.add("MISSING_SELLER_NAME");
        }

        double confidence = reasons.contains("MISSING_INVOICE_NO") || reasons.contains("MISSING_INVOICE_DATE")
                ? ZERO_CONFIDENCE
                : baseConfidence - (0.05d * reasons.size());
        item.put("confidenceReasons", reasons);
        item.put("manualInputRequired", confidence <= ZERO_CONFIDENCE);
        applyConfidence(item, confidence);
    }

    private Map<String, Object> manualInputItem(String reason) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("invoiceType", "UNKNOWN");
        item.put("confidenceReasons", new ArrayList<>(List.of(reason)));
        item.put("manualInputRequired", true);
        applyConfidence(item, ZERO_CONFIDENCE);
        return item;
    }

    @SuppressWarnings("unchecked")
    private void addConfidenceReason(Map<String, Object> item, String reason) {
        Object value = item.get("confidenceReasons");
        List<String> reasons = value instanceof List<?> list
                ? new ArrayList<>((List<String>) list)
                : new ArrayList<>();
        reasons.add(reason);
        item.put("confidenceReasons", reasons);
    }

    private void applyConfidence(Map<String, Object> item, double confidence) {
        double normalized = Math.clamp(confidence, ZERO_CONFIDENCE, MAX_CONFIDENCE);
        item.put("confidence", normalized);
        item.put("lowConfidence", normalized < lowConfidenceThreshold);
        item.put("manualInputRequired", Boolean.TRUE.equals(item.get("manualInputRequired")) || normalized <= ZERO_CONFIDENCE);
    }

    private boolean requiresVatVerification(Map<String, Object> item) {
        return item != null
                && VAT_INVOICE.equals(item.get("invoiceType"))
                && StringUtils.hasText(text(item.get("invoiceNo")))
                && StringUtils.hasText(text(item.get("invoiceDate")));
    }

    private void validateRecognizeRequest(OcrRecognizeRequestEntity request) {
        if (request == null) {
            throw new ServiceException("OCR request is required", ErrorCode.VALIDATION_ERROR);
        }
        if (!StringUtils.hasText(request.getFileBase64()) && !StringUtils.hasText(request.getFileUrl())) {
            throw new ServiceException("Either fileBase64 or fileUrl is required", ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateVerifyRequest(OcrVatVerificationRequestEntity request) {
        if (request == null) {
            throw new ServiceException("VAT verification request is required", ErrorCode.VALIDATION_ERROR);
        }
        if (!StringUtils.hasText(request.getInvoiceNo()) || !StringUtils.hasText(request.getInvoiceDate())) {
            throw new ServiceException("invoiceNo and invoiceDate are required", ErrorCode.VALIDATION_ERROR);
        }
    }

    private static String mapInvoiceType(Integer providerType) {
        if (providerType == null) {
            return "OTHER";
        }
        return switch (providerType) {
            case 2 -> "TRAIN_TICKET";
            case 3, 11, 12, 16 -> VAT_INVOICE;
            case 5 -> "FLIGHT_ITINERARY";
            case 8 -> "PAPER_INVOICE";
            default -> "OTHER";
        };
    }

    private static Map<String, Object> flattenInvoiceInfo(Map<String, Object> singleInvoiceInfos) {
        Map<String, Object> flattened = new LinkedHashMap<>();
        if (singleInvoiceInfos == null || singleInvoiceInfos.isEmpty()) {
            return flattened;
        }
        for (Object value : singleInvoiceInfos.values()) {
            Map<String, Object> nested = asMap(value);
            if (nested != null) {
                flattened.putAll(nested);
            }
        }
        if (flattened.isEmpty()) {
            flattened.putAll(singleInvoiceInfos);
        }
        return flattened;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asMapList(Object value) {
        return value instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }

    private static String firstText(Map<String, Object> values, String... keys) {
        for (String key : keys) {
            String value = text(values.get(key));
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private static BigDecimal firstMoney(Map<String, Object> values, String... keys) {
        String value = firstText(values, keys);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.replaceAll("[^0-9.-]", "").trim();
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String normalizeVerifyDate(Object value) {
        String source = text(value);
        if (!StringUtils.hasText(source)) {
            return null;
        }
        Matcher matcher = DATE_PARTS.matcher(source);
        if (matcher.matches()) {
            return formatDate(matcher.group(1), matcher.group(2), matcher.group(3), source);
        }
        String digits = source.replaceAll("\\D", "");
        if (digits.length() == 8) {
            return formatDate(digits.substring(0, 4), digits.substring(4, 6), digits.substring(6, 8), source);
        }
        return source;
    }

    private static String formatDate(String year, String month, String day, String fallback) {
        try {
            return LocalDate.of(
                    Integer.parseInt(year),
                    Integer.parseInt(month),
                    Integer.parseInt(day)
            ).format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private static String normalizeVerifyAmount(Object value) {
        String source = text(value);
        if (!StringUtils.hasText(source)) {
            return null;
        }
        String normalized = source.replaceAll("[^0-9.-]", "").trim();
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        try {
            return new BigDecimal(normalized).toPlainString();
        } catch (NumberFormatException ex) {
            return source;
        }
    }

    private static String selectVerifyAmount(Map<String, Object> item, String invoiceKind) {
        if (isFullyDigitalInvoice(item)) {
            return normalizeVerifyAmount(item.get("totalAmount"));
        }
        String amountWithoutTax = normalizeVerifyAmount(item.get("amountWithoutTax"));
        if (StringUtils.hasText(amountWithoutTax)) {
            return amountWithoutTax;
        }
        return normalizeVerifyAmount(item.get("totalAmount"));
    }

    private static String normalizeDigits(Object value) {
        String source = text(value);
        if (!StringUtils.hasText(source)) {
            return null;
        }
        String digits = source.replaceAll("\\D", "");
        return StringUtils.hasText(digits) ? digits : source;
    }

    private static String normalizeInvoiceCode(Object value) {
        String digits = normalizeDigits(value);
        if (!StringUtils.hasText(digits)) {
            return null;
        }
        return digits.length() == 10 || digits.length() == 12 ? digits : null;
    }

    private static String normalizeCheckCode(Object value) {
        String digits = normalizeDigits(value);
        if (!StringUtils.hasText(digits)) {
            return null;
        }
        return digits.length() > 6 ? digits.substring(digits.length() - 6) : digits;
    }

    private static String resolveInvoiceKind(Map<String, Object> item) {
        if (!isFullyDigitalInvoice(item)) {
            return null;
        }
        String title = text(item.get("title"));
        if (containsAny(title, "专用", "专票")) {
            return "08";
        }
        return "10";
    }

    private static String resolveSellerTaxCode(Map<String, Object> item) {
        return Boolean.TRUE.equals(resolveEnableCommonElectronic(item)) ? text(item.get("sellerTaxId")) : null;
    }

    private static Boolean resolveEnableCommonElectronic(Map<String, Object> item) {
        String description = (text(item.get("providerTypeDescription")) + " " + text(item.get("providerSubTypeDescription")) + " " + text(item.get("title"))).trim();
        return containsAny(description, "通用机打电子") ? Boolean.TRUE : null;
    }

    private static boolean isFullyDigitalInvoice(Map<String, Object> item) {
        String invoiceNo = normalizeDigits(item.get("invoiceNo"));
        if (invoiceNo != null && invoiceNo.length() == FULLY_DIGITAL_INVOICE_NO_LENGTH) {
            return true;
        }
        return containsAny(text(item.get("title")), "全电", "电子发票");
    }

    private static boolean containsAny(String value, String... candidates) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private static Integer integer(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static double number(Object value, double defaultValue) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

}
