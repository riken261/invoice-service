package cloud.techotakus.invoice.file.domain.repository;

import cloud.techotakus.invoice.file.domain.entity.FileAccessEntity;
import cloud.techotakus.invoice.file.domain.entity.FileUploadEntity;

public interface FileOperationRepository {

    void save(FileUploadEntity entity);

    FileUploadEntity findAccessibleFile(String fileId);

    void putObject(FileUploadEntity entity, String providerCode);

    FileAccessEntity presignGetObject(
            FileUploadEntity entity,
            String providerCode,
            long expiresSeconds,
            String contentDisposition,
            String accessType,
            String purpose
    );

}
