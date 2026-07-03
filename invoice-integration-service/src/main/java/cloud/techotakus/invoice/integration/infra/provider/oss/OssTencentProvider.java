package cloud.techotakus.invoice.integration.infra.provider.oss;

import cloud.techotakus.invoice.integration.api.model.OssCopyObjectRequestModel;
import cloud.techotakus.invoice.integration.api.model.OssCopyObjectResponseModel;
import cloud.techotakus.invoice.integration.api.model.OssDeleteObjectResponseModel;
import cloud.techotakus.invoice.integration.api.model.OssGetObjectResponseModel;
import cloud.techotakus.invoice.integration.api.model.OssHealthResponseModel;
import cloud.techotakus.invoice.integration.api.model.OssKeyRequestModel;
import cloud.techotakus.invoice.integration.api.model.OssPresignObjectRequestModel;
import cloud.techotakus.invoice.integration.api.model.OssPresignedUrlResponseModel;
import cloud.techotakus.invoice.integration.api.model.OssPutObjectRequestModel;
import cloud.techotakus.invoice.integration.api.model.OssPutObjectResponseModel;
import cloud.techotakus.invoice.integration.api.model.OssHealthRequestModel;
import cloud.techotakus.invoice.integration.domain.repository.OssIntegrationRepository;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.CopyObjectResult;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "invoice.integration.storage.tencent", name = "enabled", havingValue = "true")
public class OssTencentProvider implements OssIntegrationRepository {

    private static final String PROVIDER_CODE = "TENCENT_COS";
    private static final String STORAGE_TYPE = "OSS";

    @Value("${invoice.integration.storage.bucket:}")
    private String defaultBucket;

    @Value("${invoice.integration.storage.tencent.region:ap-beijing}")
    private String region;

    @Value("${invoice.integration.storage.tencent.secret-id:}")
    private String secretId;

    @Value("${invoice.integration.storage.tencent.secret-key:}")
    private String secretKey;

    @Value("${invoice.integration.storage.tencent.connection-timeout:3s}")
    private Duration connectionTimeout;

    @Value("${invoice.integration.storage.tencent.socket-timeout:10s}")
    private Duration socketTimeout;

    @Value("${invoice.integration.storage.presign-expires:5m}")
    private Duration presignExpires;

    @Override
    public String providerCode() {
        return PROVIDER_CODE;
    }

    @Override
    public String storageType() {
        return STORAGE_TYPE;
    }

    @Override
    public OssPutObjectResponseModel putObject(OssPutObjectRequestModel command) {
        validatePut(command);
        long startedAt = System.nanoTime();
        byte[] content = decode(command.contentBase64());
        String bucket = bucket(command.bucket());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.length);
        metadata.setContentType(contentType(command.contentType()));
        metadata(command.metadata(), command.tenantId(), command.fileId(), command.sha256()).forEach(metadata::addUserMetadata);

        COSClient client = client();
        try (ByteArrayInputStream input = new ByteArrayInputStream(content)) {
            PutObjectResult result = client.putObject(bucket, command.objectKey(), input, metadata);
            ObjectMetadata uploadedMetadata = result.getMetadata();
            return new OssPutObjectResponseModel(
                    true,
                    providerCode(),
                    storageType(),
                    bucket,
                    command.objectKey(),
                    trimQuotes(result.getETag()),
                    (long) content.length,
                    uploadedMetadata == null ? command.sha256() : uploadedMetadata.getUserMetaDataOf("sha256"),
                    contentType(command.contentType()),
                    metadata.getUserMetadata(),
                    elapsedMillis(startedAt)
            );
        } catch (IOException ex) {
            throw new IllegalStateException("Tencent COS input stream close failed", ex);
        } finally {
            client.shutdown();
        }
    }

    @Override
    public OssGetObjectResponseModel getObject(OssKeyRequestModel command) {
        validateKey(command);
        long startedAt = System.nanoTime();
        String bucket = bucket(command.bucket());
        COSClient client = client();
        try {
            COSObject object = client.getObject(bucket, command.objectKey());
            byte[] content = object.getObjectContent().readAllBytes();
            return result(command, bucket, object.getObjectMetadata(), Base64.getEncoder().encodeToString(content), elapsedMillis(startedAt));
        } catch (IOException ex) {
            throw new IllegalStateException("Tencent COS getObject failed", ex);
        } finally {
            client.shutdown();
        }
    }

    @Override
    public OssPresignedUrlResponseModel presignGetObject(OssPresignObjectRequestModel command) {
        validatePresign(command);
        long startedAt = System.nanoTime();
        String bucket = bucket(command.bucket());
        long expiresSeconds = command.expiresSeconds() == null ? presignExpires.toSeconds() : command.expiresSeconds();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(expiresSeconds);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, command.objectKey(), HttpMethodName.GET);
        request.setExpiration(Date.from(expiresAt.toInstant()));
        if (StringUtils.hasText(command.responseContentType())) {
            request.addRequestParameter("response-content-type", command.responseContentType());
        }
        if (StringUtils.hasText(command.responseContentDisposition())) {
            request.addRequestParameter("response-content-disposition", command.responseContentDisposition());
        }

        COSClient client = client();
        try {
            return new OssPresignedUrlResponseModel(
                    true,
                    providerCode(),
                    storageType(),
                    bucket,
                    command.objectKey(),
                    client.generatePresignedUrl(request).toString(),
                    expiresAt,
                    "GET",
                    StringUtils.hasText(command.purpose()) ? command.purpose() : "GET_OBJECT",
                    elapsedMillis(startedAt)
            );
        } finally {
            client.shutdown();
        }
    }

    @Override
    public OssGetObjectResponseModel getObjectMetadata(OssKeyRequestModel command) {
        validateKey(command);
        long startedAt = System.nanoTime();
        String bucket = bucket(command.bucket());
        COSClient client = client();
        try {
            ObjectMetadata metadata = client.getObjectMetadata(bucket, command.objectKey());
            return result(command, bucket, metadata, null, elapsedMillis(startedAt));
        } finally {
            client.shutdown();
        }
    }

    @Override
    public OssGetObjectResponseModel objectExists(OssKeyRequestModel command) {
        validateKey(command);
        long startedAt = System.nanoTime();
        String bucket = bucket(command.bucket());
        COSClient client = client();
        try {
            boolean exists = client.doesObjectExist(bucket, command.objectKey());
            if (!exists) {
                return new OssGetObjectResponseModel(true, false, providerCode(), storageType(), bucket, command.objectKey(), null, null, null, null, null, null, Map.of(), elapsedMillis(startedAt));
            }
            ObjectMetadata metadata = client.getObjectMetadata(bucket, command.objectKey());
            return result(command, bucket, metadata, null, elapsedMillis(startedAt));
        } finally {
            client.shutdown();
        }
    }

    @Override
    public OssDeleteObjectResponseModel deleteObject(OssKeyRequestModel command) {
        validateKey(command);
        long startedAt = System.nanoTime();
        String bucket = bucket(command.bucket());
        COSClient client = client();
        try {
            boolean existed = client.doesObjectExist(bucket, command.objectKey());
            client.deleteObject(bucket, command.objectKey());
            return new OssDeleteObjectResponseModel(true, providerCode(), storageType(), bucket, command.objectKey(), existed, elapsedMillis(startedAt));
        } finally {
            client.shutdown();
        }
    }

    @Override
    public OssCopyObjectResponseModel copyObject(OssCopyObjectRequestModel command) {
        validateCopy(command);
        long startedAt = System.nanoTime();
        String sourceBucket = bucket(command.sourceBucket());
        String targetBucket = bucket(command.targetBucket());
        COSClient client = client();
        try {
            CopyObjectResult result = client.copyObject(sourceBucket, command.sourceObjectKey(), targetBucket, command.targetObjectKey());
            ObjectMetadata metadata = client.getObjectMetadata(targetBucket, command.targetObjectKey());
            return new OssCopyObjectResponseModel(
                    true,
                    providerCode(),
                    storageType(),
                    sourceBucket,
                    command.sourceObjectKey(),
                    targetBucket,
                    command.targetObjectKey(),
                    trimQuotes(result.getETag()),
                    metadata.getContentLength(),
                    elapsedMillis(startedAt)
            );
        } finally {
            client.shutdown();
        }
    }

    @Override
    public OssHealthResponseModel healthCheck(OssHealthRequestModel command) {
        long startedAt = System.nanoTime();
        String bucket = bucket(command == null ? null : command.bucket());
        Map<String, Object> providerResult = new LinkedHashMap<>();
        boolean writeTest = command != null && Boolean.TRUE.equals(command.writeTestEnabled());
        COSClient client = client();
        try {
            client.getBucketLocation(bucket);
            boolean writePassed = false;
            if (writeTest) {
                String prefix = StringUtils.hasText(command.testPrefix()) ? trimSlashes(command.testPrefix()) : "health-check";
                String key = prefix + "/storage-health-" + System.currentTimeMillis() + ".txt";
                OssPutObjectRequestModel put = new OssPutObjectRequestModel(providerCode(), "health-check", "health-check", bucket, key, "text/plain", Base64.getEncoder().encodeToString("ok".getBytes(StandardCharsets.UTF_8)), 2L, null, Map.of("source", "health-check"), Map.of());
                putObject(put);
                deleteObject(new OssKeyRequestModel(providerCode(), "health-check", "health-check", bucket, key));
                writePassed = true;
            }
            providerResult.put("region", region);
            return new OssHealthResponseModel(true, providerCode(), storageType(), bucket, true, writePassed, true, elapsedMillis(startedAt), null, null, providerResult);
        } catch (Exception ex) {
            providerResult.put("exception", ex.getClass().getSimpleName());
            return new OssHealthResponseModel(false, providerCode(), storageType(), bucket, false, false, true, elapsedMillis(startedAt), "FILE_STORAGE_FAILED", "Tencent COS health check failed", providerResult);
        } finally {
            client.shutdown();
        }
    }

    private COSClient client() {
        if (!StringUtils.hasText(secretId) || !StringUtils.hasText(secretKey)) {
            throw new IllegalStateException("Tencent COS credential is not configured");
        }
        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig config = new ClientConfig(new Region(region));
        config.setConnectionTimeout((int) connectionTimeout.toMillis());
        config.setSocketTimeout((int) socketTimeout.toMillis());
        return new COSClient(credentials, config);
    }

    private OssGetObjectResponseModel result(OssKeyRequestModel command, String bucket, ObjectMetadata metadata, String contentBase64, long latencyMs) {
        return new OssGetObjectResponseModel(
                true,
                true,
                providerCode(),
                storageType(),
                bucket,
                command.objectKey(),
                contentBase64,
                metadata.getContentLength(),
                trimQuotes(metadata.getETag()),
                metadata.getUserMetaDataOf("sha256"),
                contentType(metadata.getContentType()),
                lastModified(metadata),
                metadata.getUserMetadata(),
                latencyMs
        );
    }

    private String bucket(String requestedBucket) {
        if (StringUtils.hasText(requestedBucket)) {
            return requestedBucket;
        }
        if (!StringUtils.hasText(defaultBucket)) {
            throw new IllegalArgumentException("bucket is required");
        }
        return defaultBucket;
    }

    private static void validatePut(OssPutObjectRequestModel command) {
        if (command == null) {
            throw new IllegalArgumentException("Storage putObject command is required");
        }
        if (!StringUtils.hasText(command.tenantId())) {
            throw new IllegalArgumentException("tenantId is required");
        }
        if (!StringUtils.hasText(command.fileId())) {
            throw new IllegalArgumentException("fileId is required");
        }
        if (!StringUtils.hasText(command.objectKey())) {
            throw new IllegalArgumentException("objectKey is required");
        }
        if (!StringUtils.hasText(command.contentBase64())) {
            throw new IllegalArgumentException("contentBase64 is required");
        }
    }

    private static void validateKey(OssKeyRequestModel command) {
        if (command == null) {
            throw new IllegalArgumentException("Storage key command is required");
        }
        if (!StringUtils.hasText(command.tenantId())) {
            throw new IllegalArgumentException("tenantId is required");
        }
        if (!StringUtils.hasText(command.objectKey())) {
            throw new IllegalArgumentException("objectKey is required");
        }
    }

    private static void validatePresign(OssPresignObjectRequestModel command) {
        if (command == null) {
            throw new IllegalArgumentException("Storage presign command is required");
        }
        if (!StringUtils.hasText(command.tenantId())) {
            throw new IllegalArgumentException("tenantId is required");
        }
        if (!StringUtils.hasText(command.objectKey())) {
            throw new IllegalArgumentException("objectKey is required");
        }
    }

    private static void validateCopy(OssCopyObjectRequestModel command) {
        if (command == null) {
            throw new IllegalArgumentException("Storage copy command is required");
        }
        if (!StringUtils.hasText(command.tenantId())) {
            throw new IllegalArgumentException("tenantId is required");
        }
        if (!StringUtils.hasText(command.sourceObjectKey())) {
            throw new IllegalArgumentException("sourceObjectKey is required");
        }
        if (!StringUtils.hasText(command.targetObjectKey())) {
            throw new IllegalArgumentException("targetObjectKey is required");
        }
    }

    private static byte[] decode(String contentBase64) {
        try {
            return Base64.getDecoder().decode(contentBase64);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("contentBase64 is invalid", ex);
        }
    }

    private static Map<String, String> metadata(Map<String, String> source, String tenantId, String fileId, String sha256) {
        Map<String, String> metadata = new LinkedHashMap<>();
        if (source != null) {
            metadata.putAll(source);
        }
        metadata.put("tenant-id", tenantId);
        metadata.put("file-id", fileId);
        if (StringUtils.hasText(sha256)) {
            metadata.put("sha256", sha256);
        }
        return metadata;
    }

    private static OffsetDateTime lastModified(ObjectMetadata metadata) {
        Date lastModified = metadata.getLastModified();
        Instant instant = lastModified == null ? Instant.now() : lastModified.toInstant();
        return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private static String contentType(String contentType) {
        return StringUtils.hasText(contentType) ? contentType : "application/octet-stream";
    }

    private static String trimSlashes(String value) {
        return value == null ? "" : value.replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private static String trimQuotes(String value) {
        return value == null ? null : value.replace("\"", "");
    }

    private static long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }
}
