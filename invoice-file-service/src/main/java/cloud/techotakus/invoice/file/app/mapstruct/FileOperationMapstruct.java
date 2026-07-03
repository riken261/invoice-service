package cloud.techotakus.invoice.file.app.mapstruct;

import cloud.techotakus.invoice.file.api.model.FileAccessResponseModel;
import cloud.techotakus.invoice.file.api.model.FileUploadRequestModel;
import cloud.techotakus.invoice.file.api.model.FileUploadResponseModel;
import cloud.techotakus.invoice.file.domain.entity.FileAccessEntity;
import cloud.techotakus.invoice.file.domain.entity.FileUploadEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileOperationMapstruct {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "storageType", ignore = true)
    @Mapping(target = "bucket", ignore = true)
    @Mapping(target = "originalFilename", source = "fileName")
    @Mapping(target = "contentType", source = "mimeType")
    @Mapping(target = "sizeBytes", source = "size")
    @Mapping(target = "fileStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdTrace", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedTrace", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    FileUploadEntity map(FileUploadRequestModel model);

    default FileUploadResponseModel mapUploadResponse(FileUploadEntity entity) {
        if (entity == null) {
            return null;
        }
        return new FileUploadResponseModel(entity.getId());
    }

    FileAccessResponseModel map(FileAccessEntity entity);

}
