package cloud.techotakus.invoice.file.domain.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class FileUploadEntity {
    private String id;
    private String tenantId;
    private String ownerUserId;
    private String storageType;
    private String bucket;
    private String objectKey;
    private String originalFilename;
    private String contentType;
    private Long sizeBytes;
    private String sha256;
    private String fileStatus;
    private String contentBase64;
    private String uploadNonce;
    private OffsetDateTime createdAt;
    private String createdBy;
    private String createdTrace;
    private OffsetDateTime updatedAt;
    private String updatedBy;
    private String updatedTrace;
    private Boolean deleted;
}
