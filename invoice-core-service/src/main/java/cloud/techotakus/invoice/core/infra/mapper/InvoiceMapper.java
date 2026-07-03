package cloud.techotakus.invoice.core.infra.mapper;

import cloud.techotakus.invoice.core.infra.dto.InvoiceDto;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InvoiceMapper extends BaseMapper<InvoiceDto> {
}
