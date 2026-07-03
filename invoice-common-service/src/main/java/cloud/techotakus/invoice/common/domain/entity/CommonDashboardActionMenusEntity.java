package cloud.techotakus.invoice.common.domain.entity;

import java.util.List;

public record CommonDashboardActionMenusEntity(
    List<CommonDashboardActionGroupEntity> menuList
) {
}
