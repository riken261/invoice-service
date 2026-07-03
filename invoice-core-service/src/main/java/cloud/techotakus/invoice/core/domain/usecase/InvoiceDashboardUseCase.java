package cloud.techotakus.invoice.core.domain.usecase;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.core.api.model.InvoiceDashboardEmployeeSummaryModel;
import cloud.techotakus.invoice.core.domain.repository.InvoiceDashboardRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InvoiceDashboardUseCase {

    @Resource
    private InvoiceDashboardRepository repository;

    public InvoiceDashboardEmployeeSummaryModel employeeSummary(String tenantId, String userId) {
        String normalizedTenantId = required(tenantId, "tenantId");
        String normalizedUserId = required(userId, "userId");
        return new InvoiceDashboardEmployeeSummaryModel(
                repository.countByOcrStatus(normalizedTenantId, normalizedUserId, "PROCESSING"),
                repository.countByInvoiceStatus(normalizedTenantId, normalizedUserId, "OCR_SUCCESS"),
                repository.countByInvoiceStatus(normalizedTenantId, normalizedUserId, "OCR_FAILED", "MANUAL_REVIEW_REQUIRED"),
                repository.countByInvoiceStatus(normalizedTenantId, normalizedUserId, "FINANCE_REJECTED")
        );
    }

    private static String required(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ServiceException(fieldName + " is required", ErrorCode.VALIDATION_ERROR);
        }
        return value.trim();
    }
}
