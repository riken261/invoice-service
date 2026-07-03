package cloud.techotakus.invoice.integration.domain.entity;

import lombok.Data;

@Data
public class OcrVatVerificationRequestEntity {
    private String traceId;
    private String tenantId;
    private String invoiceId;
    private String taskId;
    private String providerCode;
    private String invoiceNo;
    private String invoiceDate;
    private String invoiceCode;
    private String invoiceKind;
    private String checkCode;
    private String amount;
    private String regionCode;
    private String sellerTaxCode;
    private Boolean enableCommonElectronic;
    private Boolean enableTodayInvoice;
}
