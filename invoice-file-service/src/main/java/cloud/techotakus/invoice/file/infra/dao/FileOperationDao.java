package cloud.techotakus.invoice.file.infra.dao;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.file.domain.entity.FileAccessEntity;
import cloud.techotakus.invoice.file.domain.entity.FileUploadEntity;
import cloud.techotakus.invoice.file.domain.repository.FileOperationRepository;
import cloud.techotakus.invoice.file.infra.client.oss.FileOssIntegrationClient;
import cloud.techotakus.invoice.file.infra.client.oss.model.FileOssPresignObjectRequest;
import cloud.techotakus.invoice.file.infra.client.oss.model.FileOssPresignedUrlResponse;
import cloud.techotakus.invoice.file.infra.client.oss.model.FileOssPutObjectRequest;
import cloud.techotakus.invoice.file.infra.dto.FileUploadDto;
import cloud.techotakus.invoice.file.infra.mapper.FileUploadMapper;
import cloud.techotakus.invoice.file.infra.mapstruct.FileUploadInfraMapstruct;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class FileOperationDao extends CrudRepository<FileUploadMapper, FileUploadDto> implements FileOperationRepository {

    @Resource
    private FileUploadInfraMapstruct mapstruct;

    @Resource
    private FileOssIntegrationClient ossIntegrationClient;

    @Override
    public void save(FileUploadEntity entity) {
        save(mapstruct.map(entity));
    }

    @Override
    public FileUploadEntity findAccessibleFile(String fileId) {
        FileUploadDto dto = getBaseMapper().selectOne(new LambdaQueryWrapper<FileUploadDto>()
                .eq(FileUploadDto::getId, fileId)
                .in(FileUploadDto::getFileStatus, "ACTIVE", "TEMPORARY")
                .last("LIMIT 1"));
        return mapstruct.map(dto);
    }

    @Override
    public void putObject(FileUploadEntity entity, String providerCode) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("original-filename", entity.getOriginalFilename());
        if (StringUtils.hasText(entity.getUploadNonce())) {
            metadata.put("upload-nonce", entity.getUploadNonce());
        }
        RestResponse<?> response = ossIntegrationClient.putObject(new FileOssPutObjectRequest(
                providerCode,
                entity.getTenantId(),
                entity.getId(),
                entity.getBucket(),
                entity.getObjectKey(),
                entity.getContentType(),
                entity.getContentBase64(),
                entity.getSizeBytes(),
                entity.getSha256(),
                metadata,
                Map.of("source", "invoice-upload")
        ));
        unwrap(response, "Storage upload failed");
    }

    @Override
    public FileAccessEntity presignGetObject(
            FileUploadEntity entity,
            String providerCode,
            long expiresSeconds,
            String contentDisposition,
            String accessType,
            String purpose
    ) {
        RestResponse<FileOssPresignedUrlResponse> response = ossIntegrationClient.presignGetObject(new FileOssPresignObjectRequest(
                providerCode,
                entity.getTenantId(),
                entity.getId(),
                entity.getBucket(),
                entity.getObjectKey(),
                expiresSeconds,
                contentDisposition,
                entity.getContentType(),
                purpose
        ));
        FileOssPresignedUrlResponse data = unwrap(response, "Storage presign failed");
        if (!StringUtils.hasText(data.signedUrl())) {
            throw new ServiceException("Storage provider did not return a signed URL", ErrorCode.FILE_STORAGE_FAILED);
        }
        return new FileAccessEntity(
                entity.getId(),
                data.signedUrl(),
                StringUtils.hasText(data.method()) ? data.method() : "GET",
                Math.toIntExact(Math.min(Integer.MAX_VALUE, expiresSeconds)),
                data.expiresAt(),
                entity.getOriginalFilename(),
                entity.getContentType(),
                accessType
        );
    }

    private static <T> T unwrap(RestResponse<T> response, String defaultMessage) {
        if (response != null && response.isSuccess()) {
            return response.getData();
        }
        String message = response == null || !StringUtils.hasText(response.getError())
                ? defaultMessage
                : response.getError();
        throw new ServiceException(message, ErrorCode.FILE_STORAGE_FAILED);
    }
}
