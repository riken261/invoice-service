package cloud.techotakus.invoice.common.infra.mapper;

import cloud.techotakus.invoice.common.infra.dto.CommonDashboardActionDto;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommonDashboardActionMapper extends BaseMapper<CommonDashboardActionDto> {
}
