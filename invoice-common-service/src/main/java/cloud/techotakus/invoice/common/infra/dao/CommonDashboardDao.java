package cloud.techotakus.invoice.common.infra.dao;

import cloud.techotakus.invoice.common.domain.entity.CommonDashboardActionEntity;
import cloud.techotakus.invoice.common.domain.repository.CommonDashboardRepository;
import cloud.techotakus.invoice.common.infra.dto.CommonDashboardActionDto;
import cloud.techotakus.invoice.common.infra.mapper.CommonDashboardActionMapper;
import cloud.techotakus.invoice.common.infra.mapstruct.CommonDashboardInfraMapstruct;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CommonDashboardDao implements CommonDashboardRepository {

    private final CommonDashboardActionMapper mapper;
    private final CommonDashboardInfraMapstruct mapstruct;

    public CommonDashboardDao(CommonDashboardActionMapper mapper, CommonDashboardInfraMapstruct mapstruct) {
        this.mapper = mapper;
        this.mapstruct = mapstruct;
    }

    @Override
    public List<CommonDashboardActionEntity> enabledActions() {
        return mapstruct.map(mapper.selectList(new LambdaQueryWrapper<CommonDashboardActionDto>()
            .eq(CommonDashboardActionDto::getEnabled, true)
            .orderByAsc(CommonDashboardActionDto::getGroupCode)
            .orderByAsc(CommonDashboardActionDto::getActionCode)));
    }
}
