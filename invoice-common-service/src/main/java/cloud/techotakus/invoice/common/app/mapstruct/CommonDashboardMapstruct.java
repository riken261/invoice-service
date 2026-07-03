package cloud.techotakus.invoice.common.app.mapstruct;

import cloud.techotakus.invoice.common.api.model.CommonDashboardActionMenusResponseModel;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardActionMenusEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommonDashboardMapstruct {

    CommonDashboardActionMenusResponseModel map(CommonDashboardActionMenusEntity entity);
}
