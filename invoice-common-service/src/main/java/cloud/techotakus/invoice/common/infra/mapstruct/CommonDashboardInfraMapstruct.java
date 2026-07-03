package cloud.techotakus.invoice.common.infra.mapstruct;

import cloud.techotakus.invoice.common.domain.entity.CommonDashboardActionEntity;
import cloud.techotakus.invoice.common.infra.dto.CommonDashboardActionDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommonDashboardInfraMapstruct {

    CommonDashboardActionEntity map(CommonDashboardActionDto dto);

    List<CommonDashboardActionEntity> map(List<CommonDashboardActionDto> list);
}
