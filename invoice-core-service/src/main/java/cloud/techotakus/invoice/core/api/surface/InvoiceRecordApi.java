package cloud.techotakus.invoice.core.api.surface;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.core.api.model.InvoiceDetailResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoiceDownloadResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoiceDownloadRequestModel;
import cloud.techotakus.invoice.core.api.model.InvoicePreviewResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoiceSummaryModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/bff/v1/invoices")
public interface InvoiceRecordApi {

    @GetMapping("/list")
    ResponseEntity<PageResponse<InvoiceSummaryModel>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest httpRequest
    );

    @GetMapping("/{invoiceId}")
    ResponseEntity<RestResponse<InvoiceDetailResponseModel>> getOne(
            @PathVariable @NotBlank String invoiceId,
            HttpServletRequest httpRequest
    );

    @GetMapping("/{invoiceId}/preview")
    ResponseEntity<RestResponse<InvoicePreviewResponseModel>> preview(
            @PathVariable @NotBlank String invoiceId,
            HttpServletRequest httpRequest
    );

    @PostMapping("/download")
    ResponseEntity<RestResponse<InvoiceDownloadResponseModel>> download(
            @Valid @RequestBody InvoiceDownloadRequestModel request,
            HttpServletRequest httpRequest
    );

}
