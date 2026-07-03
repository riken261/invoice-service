package cloud.techotakus.invoice.common.api.model;

import java.util.List;

public record CommonDashboardActionGroupModel(
    String code,
    String name,
    List<CommonDashboardActionItemModel> actions
) {
}
