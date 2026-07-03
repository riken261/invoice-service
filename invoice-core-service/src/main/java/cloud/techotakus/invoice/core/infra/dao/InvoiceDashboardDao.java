package cloud.techotakus.invoice.core.infra.dao;

import cloud.techotakus.invoice.core.domain.repository.InvoiceDashboardRepository;
import cloud.techotakus.invoice.core.infra.dto.InvoiceDto;
import cloud.techotakus.invoice.core.infra.mapper.InvoiceMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class InvoiceDashboardDao implements InvoiceDashboardRepository {

    @Resource
    private InvoiceMapper invoiceMapper;

    @Override
    public long countByOcrStatus(String tenantId, String userId, String status) {
        return invoiceMapper.selectCount(new LambdaQueryWrapper<InvoiceDto>()
                .eq(InvoiceDto::getTenantId, tenantId)
                .eq(InvoiceDto::getOwnerUserId, userId)
                .eq(InvoiceDto::getOcrStatus, status)
                .eq(InvoiceDto::getDeleted, false));
    }

    @Override
    public long countByInvoiceStatus(String tenantId, String userId, String... statuses) {
        return invoiceMapper.selectCount(new LambdaQueryWrapper<InvoiceDto>()
                .eq(InvoiceDto::getTenantId, tenantId)
                .eq(InvoiceDto::getOwnerUserId, userId)
                .in(InvoiceDto::getInvoiceStatus, (Object[]) statuses)
                .eq(InvoiceDto::getDeleted, false));
    }
}
