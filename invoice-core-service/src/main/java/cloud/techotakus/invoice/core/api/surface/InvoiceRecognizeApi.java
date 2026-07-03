package cloud.techotakus.invoice.core.api.surface;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.core.api.model.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/bff/v1/invoices")
public interface InvoiceRecognizeApi {

    @PostMapping("/recognize-upload")
    ResponseEntity<RestResponse<InvoiceRecognizeUploadResponseModel>> recognizeUpload(
            @Valid @RequestBody InvoiceRecognizeUploadRequestModel request,
            HttpServletRequest httpRequest
    );

    @GetMapping("/recognize-result")
    ResponseEntity<RestResponse<InvoiceRecognizeResultResponseModel>> recognizeResult(
            @NotBlank @RequestParam String sessionId
    );

    @GetMapping("/recognize-results")
    ResponseEntity<RestResponse<InvoiceRecognizeBatchResultResponseModel>> recognizeResults(
            @NotBlank @RequestParam String batchId
    );

    @GetMapping("/ocr-sessions/{recognitionSessionId}/preview")
    ResponseEntity<RestResponse<InvoicePreviewResponseModel>> previewSession(
            @NotBlank @PathVariable String recognitionSessionId,
            HttpServletRequest httpRequest
    );

    @PostMapping("/ocr-sessions/{recognitionSessionId}/confirm")
    ResponseEntity<RestResponse<InvoiceConfirmResponseModel>> confirmSession(
            @NotBlank @PathVariable String recognitionSessionId,
            @Valid @RequestBody InvoiceConfirmRequestModel request,
            HttpServletRequest httpRequest
    );

    @PostMapping("/ocr-sessions/{recognitionSessionId}/manual-input/submit")
    ResponseEntity<RestResponse<InvoiceConfirmResponseModel>> submitSessionManualInput(
            @NotBlank @PathVariable String recognitionSessionId,
            @Valid @RequestBody InvoiceManualInputSubmitRequestModel request,
            HttpServletRequest httpRequest
    );

    @PostMapping("/{invoiceId}/manual-input/submit")
    ResponseEntity<RestResponse<InvoiceConfirmResponseModel>> submitManualInput(
            @NotBlank @PathVariable String invoiceId,
            @Valid @RequestBody InvoiceManualInputSubmitRequestModel request,
            HttpServletRequest httpRequest
    );

}
