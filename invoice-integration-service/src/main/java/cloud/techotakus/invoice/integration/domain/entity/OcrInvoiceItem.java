package cloud.techotakus.invoice.integration.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class OcrInvoiceItem {
    private String code;
    private String providerCode;
    private Integer providerType;
    private String providerSubType;
    private String providerTypeDescription;
    private String providerSubTypeDescription;
    private String invoiceType;
    private Integer page;
    private Float angle;
    private String title;
    private String invoiceCode;
    private String invoiceNo;
    private String invoiceDate;
    private String checkCode;
    private String buyerName;
    private String buyerTaxId;
    private String sellerName;
    private String sellerTaxId;
    private BigDecimal amountWithoutTax;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String currency;
    private Double confidence;
    private Boolean lowConfidence;
    private OcrPolygon polygon;
    private List<OcrItemPolygon> itemPolygons = new ArrayList<>();
    private List<OcrField> fields = new ArrayList<>();
    private List<OcrLineItem> lineItems = new ArrayList<>();
    private String qrCode;
    private OcrSealInfo sealInfo;
    private String cutImageBase64;
    private Map<String, Object> rawInvoiceInfo = new LinkedHashMap<>();
}
