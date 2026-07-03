package cloud.techotakus.invoice.claim.infra.mapper;

import cloud.techotakus.invoice.claim.infra.dto.ClaimDto;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClaimMapper extends BaseMapper<ClaimDto> {
}
