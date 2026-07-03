package cloud.techotakus.invoice.ocr.api.model;

public record OcrVatVerificationRequestModel(
        String traceId,
        String tenantId,
        String invoiceId,
        String taskId,
        String providerCode,
        String invoiceNo,
        String invoiceDate,
        String invoiceCode,
        String invoiceKind,
        String checkCode,
        String amount,
        String regionCode,
        String sellerTaxCode,
        Boolean enableCommonElectronic,
        Boolean enableTodayInvoice
) {
}
