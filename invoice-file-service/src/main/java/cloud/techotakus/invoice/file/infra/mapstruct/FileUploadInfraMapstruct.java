package cloud.techotakus.invoice.file.infra.mapstruct;

import cloud.techotakus.invoice.file.domain.entity.FileUploadEntity;
import cloud.techotakus.invoice.file.infra.dto.FileUploadDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileUploadInfraMapstruct {

    @Mapping(target = "contentBase64", ignore = true)
    @Mapping(target = "uploadNonce", ignore = true)
    FileUploadEntity map(FileUploadDto dto);

    FileUploadDto map(FileUploadEntity entity);

    List<FileUploadEntity> map(List<FileUploadDto> list);
}
