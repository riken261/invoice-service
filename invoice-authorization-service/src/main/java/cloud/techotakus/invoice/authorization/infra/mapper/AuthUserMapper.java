package cloud.techotakus.invoice.authorization.infra.mapper;

import cloud.techotakus.invoice.authorization.infra.dto.AuthUserDto;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthUserMapper extends BaseMapper<AuthUserDto> {
}
