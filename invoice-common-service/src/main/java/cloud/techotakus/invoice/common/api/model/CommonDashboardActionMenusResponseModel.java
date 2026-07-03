package cloud.techotakus.invoice.common.api.model;

import java.util.List;

public record CommonDashboardActionMenusResponseModel(
    List<CommonDashboardActionGroupModel> menuList
) {
}
