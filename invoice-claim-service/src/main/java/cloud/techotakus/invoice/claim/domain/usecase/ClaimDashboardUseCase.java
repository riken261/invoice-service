package cloud.techotakus.invoice.claim.domain.usecase;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.claim.api.model.ClaimDashboardEmployeeSummaryModel;
import cloud.techotakus.invoice.claim.domain.repository.ClaimDashboardRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ClaimDashboardUseCase {

    @Resource
    private ClaimDashboardRepository repository;

    public ClaimDashboardEmployeeSummaryModel employeeSummary(String tenantId, String userId) {
        String normalizedTenantId = required(tenantId, "tenantId");
        String normalizedUserId = required(userId, "userId");
        return new ClaimDashboardEmployeeSummaryModel(
                repository.countByStatus(normalizedTenantId, normalizedUserId, "DRAFT"),
                repository.countByStatus(normalizedTenantId, normalizedUserId, "REJECTED")
        );
    }

    private static String required(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ServiceException(fieldName + " is required", ErrorCode.VALIDATION_ERROR);
        }
        return value.trim();
    }
}
