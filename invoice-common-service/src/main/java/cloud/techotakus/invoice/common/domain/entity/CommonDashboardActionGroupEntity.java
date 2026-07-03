package cloud.techotakus.invoice.common.domain.entity;

import java.util.List;

public record CommonDashboardActionGroupEntity(
    String code,
    String name,
    List<CommonDashboardActionItemEntity> actions
) {
}
