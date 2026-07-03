package cloud.techotakus.invoice.claim.infra.dao;

import cloud.techotakus.invoice.claim.domain.repository.ClaimDashboardRepository;
import cloud.techotakus.invoice.claim.infra.dto.ClaimDto;
import cloud.techotakus.invoice.claim.infra.mapper.ClaimMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class ClaimDashboardDao implements ClaimDashboardRepository {

    @Resource
    private ClaimMapper claimMapper;

    @Override
    public long countByStatus(String tenantId, String userId, String status) {
        return claimMapper.selectCount(new LambdaQueryWrapper<ClaimDto>()
                .eq(ClaimDto::getTenantId, tenantId)
                .eq(ClaimDto::getApplicantUserId, userId)
                .eq(ClaimDto::getStatus, status)
                .eq(ClaimDto::getDeleted, false));
    }
}
