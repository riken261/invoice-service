package cloud.techotakus.invoice.review.infra.dao;

import cloud.techotakus.invoice.review.domain.repository.ReviewDashboardRepository;
import cloud.techotakus.invoice.review.infra.dto.ReviewClaimDto;
import cloud.techotakus.invoice.review.infra.dto.ReviewInvoiceDto;
import cloud.techotakus.invoice.review.infra.mapper.ReviewClaimMapper;
import cloud.techotakus.invoice.review.infra.mapper.ReviewInvoiceMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Repository
public class ReviewDashboardDao implements ReviewDashboardRepository {

    @Resource
    private ReviewInvoiceMapper invoiceMapper;

    @Resource
    private ReviewClaimMapper claimMapper;

    @Override
    public long countInvoicesByStatus(String tenantId, String... statuses) {
        return invoiceMapper.selectCount(new LambdaQueryWrapper<ReviewInvoiceDto>()
                .eq(ReviewInvoiceDto::getTenantId, tenantId)
                .in(ReviewInvoiceDto::getInvoiceStatus, (Object[]) statuses)
                .eq(ReviewInvoiceDto::getDeleted, false));
    }

    @Override
    public long countInvoicesByDuplicateStatus(String tenantId, String... statuses) {
        return invoiceMapper.selectCount(new LambdaQueryWrapper<ReviewInvoiceDto>()
                .eq(ReviewInvoiceDto::getTenantId, tenantId)
                .in(ReviewInvoiceDto::getDuplicateStatus, (Object[]) statuses)
                .eq(ReviewInvoiceDto::getDeleted, false));
    }

    @Override
    public long countClaimsByStatus(String tenantId, String... statuses) {
        return claimMapper.selectCount(new LambdaQueryWrapper<ReviewClaimDto>()
                .eq(ReviewClaimDto::getTenantId, tenantId)
                .in(ReviewClaimDto::getStatus, (Object[]) statuses)
                .eq(ReviewClaimDto::getDeleted, false));
    }

    @Override
    public String averagePendingClaimWaitingHours(String tenantId) {
        BigDecimal value = claimMapper.averagePendingClaimWaitingHours(tenantId);
        return (value == null ? BigDecimal.ZERO : value)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
    }
}
