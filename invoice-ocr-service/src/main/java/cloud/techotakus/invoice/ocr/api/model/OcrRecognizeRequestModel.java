package cloud.techotakus.invoice.ocr.api.model;

public record OcrRecognizeRequestModel(
        String traceId,
        String tenantId,
        String invoiceId,
        String taskId,
        String idempotencyKey,
        String providerCode,
        String fileId,
        String fileName,
        String fileMimeType,
        String objectKey,
        String fileBase64,
        String fileUrl,
        String invoiceTypeHint,
        Boolean enablePdf,
        Integer pdfPageNumber,
        Boolean enableMultiplePage,
        Boolean enableCutImage,
        Boolean enableItemPolygon,
        Boolean enableQrCode,
        Boolean enableSeal
) {
}
