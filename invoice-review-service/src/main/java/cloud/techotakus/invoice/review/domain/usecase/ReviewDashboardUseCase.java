package cloud.techotakus.invoice.review.domain.usecase;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.review.api.model.ReviewDashboardFinanceSummaryModel;
import cloud.techotakus.invoice.review.domain.repository.ReviewDashboardRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ReviewDashboardUseCase {

    @Resource
    private ReviewDashboardRepository repository;

    public ReviewDashboardFinanceSummaryModel financeSummary(String tenantId, String reviewerId) {
        String normalizedTenantId = required(tenantId, "tenantId");
        required(reviewerId, "reviewerId");
        return new ReviewDashboardFinanceSummaryModel(
                repository.countInvoicesByStatus(normalizedTenantId, "USER_CONFIRMED", "MANUAL_REVIEW_REQUIRED"),
                repository.countInvoicesByDuplicateStatus(normalizedTenantId, "POSSIBLE_DUPLICATE", "CONFIRMED_DUPLICATE"),
                repository.countClaimsByStatus(normalizedTenantId, "SUBMITTED", "FINANCE_REVIEWING"),
                repository.averagePendingClaimWaitingHours(normalizedTenantId)
        );
    }

    private static String required(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ServiceException(fieldName + " is required", ErrorCode.VALIDATION_ERROR);
        }
        return value.trim();
    }
}
