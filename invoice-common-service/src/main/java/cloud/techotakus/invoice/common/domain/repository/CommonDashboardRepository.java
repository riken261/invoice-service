package cloud.techotakus.invoice.common.domain.repository;

import cloud.techotakus.invoice.common.domain.entity.CommonDashboardActionEntity;
import java.util.List;

public interface CommonDashboardRepository {

    List<CommonDashboardActionEntity> enabledActions();
}
