package cloud.techotakus.invoice.core.domain.usecase;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.core.domain.entity.InvoiceEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceFileAccessEntity;
import cloud.techotakus.invoice.core.domain.repository.InvoiceRecordRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class InvoiceRecordUseCase {

    @Resource
    private InvoiceRecordRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<InvoiceEntity> list(String tenantId, String userId, int page, int pageSize) {
        return repository.list(
                required(tenantId, "tenantId"),
                required(userId, "userId"),
                normalizePage(page),
                normalizePageSize(pageSize)
        );
    }

    @Transactional(readOnly = true)
    public InvoiceEntity getOne(String tenantId, String userId, String invoiceId) {
        return loadOwnedInvoice(tenantId, userId, invoiceId);
    }

    @Transactional(readOnly = true)
    public InvoiceFileAccessEntity preview(String tenantId, String userId, String invoiceId) {
        InvoiceEntity invoice = loadOwnedInvoice(tenantId, userId, invoiceId);
        InvoiceFileAccessEntity access = repository.previewFile(requiredInvoiceFileId(invoice));
        access.setInvoiceId(invoice.getId());
        return access;
    }

    @Transactional(readOnly = true)
    public List<InvoiceFileAccessEntity> download(String tenantId, String userId, List<String> invoiceIds) {
        if (invoiceIds == null || invoiceIds.isEmpty()) {
            throw new ServiceException("invoiceIds is required", ErrorCode.VALIDATION_ERROR);
        }
        return invoiceIds.stream()
                .map(invoiceId -> {
                    InvoiceEntity invoice = loadOwnedInvoice(tenantId, userId, invoiceId);
                    InvoiceFileAccessEntity access = repository.downloadFile(requiredInvoiceFileId(invoice));
                    access.setInvoiceId(invoice.getId());
                    return access;
                })
                .toList();
    }

    private InvoiceEntity loadOwnedInvoice(String tenantId, String userId, String invoiceId) {
        InvoiceEntity invoice = repository.findOne(
                required(tenantId, "tenantId"),
                required(userId, "userId"),
                required(invoiceId, "invoiceId")
        );
        if (invoice == null) {
            throw new ServiceException("Invoice not found", ErrorCode.RESOURCE_NOT_FOUND);
        }
        return invoice;
    }

    private static String requiredInvoiceFileId(InvoiceEntity invoice) {
        if (!StringUtils.hasText(invoice.getInvoiceFileId())) {
            throw new ServiceException("Invoice file not found", ErrorCode.RESOURCE_NOT_FOUND);
        }
        return invoice.getInvoiceFileId().trim();
    }

    private static String required(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ServiceException(fieldName + " is required", ErrorCode.VALIDATION_ERROR);
        }
        return value.trim();
    }

    private static int normalizePage(int page) {
        return Math.max(page, 1);
    }

    private static int normalizePageSize(int pageSize) {
        return Math.clamp(pageSize, 1, 100);
    }
}
