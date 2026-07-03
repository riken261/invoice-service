package cloud.techotakus.invoice.file.domain.usecase;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.file.domain.entity.FileAccessEntity;
import cloud.techotakus.invoice.file.domain.entity.FileUploadEntity;
import cloud.techotakus.invoice.file.domain.repository.FileOperationRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileOperationUseCase {

    private static final String SYSTEM_ACTOR = "FILE_SERVICE";
    private static final String FILE_STATUS_ACTIVE = "ACTIVE";

    @Resource
    private FileOperationRepository repository;

    @Value("${invoice.file.storage.provider:LOCAL}")
    private String storageProvider;

    @Value("${invoice.file.storage.type:LOCAL}")
    private String storageType;

    @Value("${invoice.file.storage.bucket:invoice-files}")
    private String bucket;

    @Value("${invoice.file.max-size-bytes:20971520}")
    private long maxSizeBytes;

    @Value("${invoice.file.allowed-content-types:application/pdf,image/jpeg,image/png}")
    private Set<String> allowedContentTypes;

    @Value("${invoice.file.access.preview-expires-seconds:600}")
    private long previewExpiresSeconds;

    @Value("${invoice.file.access.download-expires-seconds:300}")
    private long downloadExpiresSeconds;

    @Transactional
    public FileUploadEntity upload(FileUploadEntity entity) {
        validateUpload(entity);
        OffsetDateTime now = OffsetDateTime.now();
        entity.setId(newId());
        entity.setStorageType(normalizeStorageType(storageType));
        entity.setBucket(normalizeBlank(bucket));
        entity.setObjectKey(normalizeObjectKey(entity.getObjectKey()));
        entity.setOriginalFilename(normalizeFilename(entity.getOriginalFilename()));
        entity.setContentType(normalizeContentType(entity.getContentType()));
        entity.setSha256(normalizeSha256(entity.getSha256()));
        entity.setFileStatus(FILE_STATUS_ACTIVE);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(entity.getOwnerUserId());
        entity.setUpdatedBy(SYSTEM_ACTOR);
        entity.setCreatedTrace(normalizeBlank(entity.getUploadNonce()));
        entity.setDeleted(false);

        repository.save(entity);
        if (StringUtils.hasText(entity.getContentBase64())) {
            repository.putObject(entity, normalizeUpper(storageProvider));
        }
        return entity;
    }

    @Transactional(readOnly = true)
    public FileAccessEntity preview(String fileId) {
        FileUploadEntity file = getAccessibleFile(fileId);
        return access(file, previewExpiresSeconds, "inline", "PREVIEW", "PREVIEW_OBJECT");
    }

    @Transactional(readOnly = true)
    public FileAccessEntity download(String fileId) {
        FileUploadEntity file = getAccessibleFile(fileId);
        return access(
                file,
                downloadExpiresSeconds,
                "attachment; filename=\"" + asciiFilename(file.getOriginalFilename()) + "\"",
                "DOWNLOAD",
                "DOWNLOAD_OBJECT"
        );
    }

    private FileUploadEntity getAccessibleFile(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            throw new ServiceException("fileId is required", ErrorCode.VALIDATION_ERROR);
        }
        FileUploadEntity file = repository.findAccessibleFile(fileId.trim());
        if (file == null) {
            throw new ServiceException("File not found: " + fileId, ErrorCode.RESOURCE_NOT_FOUND);
        }
        return file;
    }

    private FileAccessEntity access(
            FileUploadEntity file,
            long expiresSeconds,
            String contentDisposition,
            String accessType,
            String purpose
    ) {
        return repository.presignGetObject(
                file,
                normalizeUpper(storageProvider),
                expiresSeconds,
                contentDisposition,
                accessType,
                purpose
        );
    }

    private void validateUpload(FileUploadEntity entity) {
        if (entity == null) {
            throw new ServiceException("File upload request is required", ErrorCode.VALIDATION_ERROR);
        }
        entity.setTenantId(requiredTrim(entity.getTenantId(), "tenantId"));
        entity.setOwnerUserId(requiredTrim(entity.getOwnerUserId(), "ownerUserId"));
        normalizeFilename(entity.getOriginalFilename());
        normalizeContentType(entity.getContentType());
        if (entity.getSizeBytes() == null || entity.getSizeBytes() <= 0) {
            throw new ServiceException("size must be positive", ErrorCode.VALIDATION_ERROR);
        }
        if (entity.getSizeBytes() > maxSizeBytes) {
            throw new ServiceException("File is too large", ErrorCode.FILE_TOO_LARGE);
        }
        String contentType = normalizeContentType(entity.getContentType());
        if (allowedContentTypes != null && !allowedContentTypes.isEmpty() && !allowedContentTypes.contains(contentType)) {
            throw new ServiceException("File type is not allowed: " + contentType, ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        normalizeSha256(entity.getSha256());
        normalizeObjectKey(entity.getObjectKey());
    }

    private static String requiredTrim(String value, String fieldName) {
        String normalized = normalizeBlank(value);
        if (!StringUtils.hasText(normalized)) {
            throw new ServiceException(fieldName + " is required", ErrorCode.VALIDATION_ERROR);
        }
        return normalized;
    }

    private static String normalizeObjectKey(String value) {
        String normalized = normalizeBlank(value);
        if (!StringUtils.hasText(normalized)) {
            throw new ServiceException("objectKey is required", ErrorCode.VALIDATION_ERROR);
        }
        String sanitized = normalized.replace('\\', '/');
        if (sanitized.startsWith("/") || hasParentTraversal(sanitized)) {
            throw new ServiceException("objectKey is invalid", ErrorCode.VALIDATION_ERROR);
        }
        return sanitized;
    }

    private static String normalizeFilename(String value) {
        String normalized = normalizeBlank(value);
        if (!StringUtils.hasText(normalized)) {
            throw new ServiceException("fileName is required", ErrorCode.VALIDATION_ERROR);
        }
        String filename = normalized.replace('\\', '/');
        int lastSlash = filename.lastIndexOf('/');
        if (lastSlash >= 0) {
            filename = filename.substring(lastSlash + 1);
        }
        if (!StringUtils.hasText(filename)) {
            throw new ServiceException("fileName is invalid", ErrorCode.VALIDATION_ERROR);
        }
        return filename;
    }

    private static String normalizeContentType(String value) {
        String normalized = normalizeBlank(value);
        if (!StringUtils.hasText(normalized)) {
            throw new ServiceException("mimeType is required", ErrorCode.VALIDATION_ERROR);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static String normalizeSha256(String value) {
        String normalized = normalizeBlank(value);
        if (!StringUtils.hasText(normalized) || !normalized.matches("(?i)[0-9a-f]{64}")) {
            throw new ServiceException("sha256 is invalid", ErrorCode.VALIDATION_ERROR);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static String normalizeUpper(String value) {
        String normalized = normalizeBlank(value);
        return StringUtils.hasText(normalized) ? normalized.toUpperCase(Locale.ROOT) : "";
    }

    private static String normalizeStorageType(String value) {
        String normalized = normalizeUpper(value);
        return switch (normalized) {
            case "COS", "TENCENT_COS", "TENCENT" -> "OSS";
            default -> normalized;
        };
    }

    private static String normalizeBlank(String value) {
        return value == null ? null : value.trim();
    }

    private static String asciiFilename(String filename) {
        String normalized = Normalizer.normalize(filename, Normalizer.Form.NFKD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replace("\\", "")
                .replace("\"", "");
        return StringUtils.hasText(normalized) ? normalized : "invoice-file";
    }

    private static boolean hasParentTraversal(String objectKey) {
        for (String segment : objectKey.split("/")) {
            if ("..".equals(segment)) {
                return true;
            }
        }
        return false;
    }

    private static String newId() {
        return "file_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + UUID.randomUUID().toString().replace("-", "").substring(0,5);
    }

}
