package cloud.techotakus.invoice.file.infra.mapper;

import cloud.techotakus.invoice.file.infra.dto.FileUploadDto;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileUploadMapper extends BaseMapper<FileUploadDto> {
}
