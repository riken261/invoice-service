package cloud.techotakus.invoice.claim.infra.mapper;

import cloud.techotakus.invoice.claim.infra.dto.ClaimItemDto;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClaimItemMapper extends BaseMapper<ClaimItemDto> {
}
