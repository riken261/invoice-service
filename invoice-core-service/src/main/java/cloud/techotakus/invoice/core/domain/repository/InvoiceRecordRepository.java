package cloud.techotakus.invoice.core.domain.repository;

import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.core.domain.entity.InvoiceEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceFileAccessEntity;

public interface InvoiceRecordRepository {

    PageResponse<InvoiceEntity> list(String tenantId, String userId, int page, int pageSize);

    InvoiceEntity findOne(String tenantId, String userId, String invoiceId);

    InvoiceFileAccessEntity previewFile(String fileId);

    InvoiceFileAccessEntity downloadFile(String fileId);
}
