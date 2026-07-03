package cloud.techotakus.invoice.core.app.controller;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.core.api.model.InvoiceDetailResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoiceDownloadRequestModel;
import cloud.techotakus.invoice.core.api.model.InvoiceDownloadResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoicePreviewResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoiceSummaryModel;
import cloud.techotakus.invoice.core.api.surface.InvoiceRecordApi;
import cloud.techotakus.invoice.core.app.mapstruct.InvoiceRecordMapstruct;
import cloud.techotakus.invoice.core.domain.usecase.InvoiceRecordUseCase;
import cloud.techotakus.invoice.starters.support.InvoiceGatewayContextResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Invoice Record", description = "Invoice record BFF APIs")
public class InvoiceRecordController implements InvoiceRecordApi {

    @Resource
    private InvoiceRecordUseCase useCase;

    @Resource
    private InvoiceRecordMapstruct mapstruct;

    @Resource
    private InvoiceGatewayContextResolver gatewayContextResolver;

    @Override
    public ResponseEntity<PageResponse<InvoiceSummaryModel>> list(
            int page,
            int pageSize,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        var result = useCase.list(context.tenantId(), context.userId(), page, pageSize);
        var response = PageResponse.success(
                result.getItems().stream().map(mapstruct::mapSummaryModel).toList(),
                result.getPage(),
                result.getPageSize(),
                result.getTotal()
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<RestResponse<InvoiceDetailResponseModel>> getOne(
            String invoiceId,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapDetail(
                useCase.getOne(context.tenantId(), context.userId(), invoiceId)
        )));
    }

    @Override
    public ResponseEntity<RestResponse<InvoicePreviewResponseModel>> preview(
            String invoiceId,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapPreview(
                useCase.preview(context.tenantId(), context.userId(), invoiceId)
        )));
    }

    @Override
    public ResponseEntity<RestResponse<InvoiceDownloadResponseModel>> download(
            InvoiceDownloadRequestModel request,
            HttpServletRequest httpRequest
    ) {
        var context = gatewayContextResolver.currentContext(httpRequest);
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapDownload(
                useCase.download(context.tenantId(), context.userId(), request == null ? null : request.invoiceIds())
        )));
    }
}
