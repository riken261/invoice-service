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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "invoice.integration.storage.local", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OssLocalProvider implements OssIntegrationRepository {

    private static final String PROVIDER_CODE = "LOCAL";
    private static final String STORAGE_TYPE = "LOCAL";

    @Value("${invoice.integration.storage.bucket:invoice-files-dev}")
    private String defaultBucket;

    @Value("${invoice.integration.storage.local-root:build/storage/invoice-files}")
    private String localRoot;

    @Value("${invoice.integration.storage.public-base-url:}")
    private String publicBaseUrl;

    @Value("${invoice.integration.storage.presign-expires:10m}")
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
        Path path = resolve(bucket, command.objectKey());
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, content);
            String sha256 = sha256Hex(content);
            return new OssPutObjectResponseModel(
                    true,
                    providerCode(),
                    storageType(),
                    bucket,
                    command.objectKey(),
                    sha256,
                    (long) content.length,
                    sha256,
                    contentType(command.contentType()),
                    metadata(command.metadata(), command.tenantId(), command.fileId(), sha256),
                    elapsedMillis(startedAt)
            );
        } catch (IOException ex) {
            throw storageFailed("Local storage putObject failed", ex);
        }
    }

    @Override
    public OssGetObjectResponseModel getObject(OssKeyRequestModel command) {
        validateKey(command);
        long startedAt = System.nanoTime();
        ObjectState state = state(command.bucket(), command.objectKey());
        if (!state.exists()) {
            throw notFound(state.bucket(), command.objectKey());
        }
        try {
            byte[] content = Files.readAllBytes(state.path());
            return result(command, state, Base64.getEncoder().encodeToString(content), elapsedMillis(startedAt));
        } catch (IOException ex) {
            throw storageFailed("Local storage getObject failed", ex);
        }
    }

    @Override
    public OssPresignedUrlResponseModel presignGetObject(OssPresignObjectRequestModel command) {
        validatePresign(command);
        long startedAt = System.nanoTime();
        ObjectState state = state(command.bucket(), command.objectKey());
        if (!state.exists()) {
            throw notFound(state.bucket(), command.objectKey());
        }
        long expiresSeconds = command.expiresSeconds() == null
                ? presignExpires.toSeconds()
                : command.expiresSeconds();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(expiresSeconds);
        String signedUrl = localUrl(state.bucket(), command.objectKey(), expiresAt);
        return new OssPresignedUrlResponseModel(
                true,
                providerCode(),
                storageType(),
                state.bucket(),
                command.objectKey(),
                signedUrl,
                expiresAt,
                "GET",
                StringUtils.hasText(command.purpose()) ? command.purpose() : "GET_OBJECT",
                elapsedMillis(startedAt)
        );
    }

    @Override
    public OssGetObjectResponseModel getObjectMetadata(OssKeyRequestModel command) {
        validateKey(command);
        long startedAt = System.nanoTime();
        ObjectState state = state(command.bucket(), command.objectKey());
        if (!state.exists()) {
            throw notFound(state.bucket(), command.objectKey());
        }
        return result(command, state, null, elapsedMillis(startedAt));
    }

    @Override
    public OssGetObjectResponseModel objectExists(OssKeyRequestModel command) {
        validateKey(command);
        long startedAt = System.nanoTime();
        ObjectState state = state(command.bucket(), command.objectKey());
        if (!state.exists()) {
            return new OssGetObjectResponseModel(
                    true,
                    false,
                    providerCode(),
                    storageType(),
                    state.bucket(),
                    command.objectKey(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Map.of(),
                    elapsedMillis(startedAt)
            );
        }
        return result(command, state, null, elapsedMillis(startedAt));
    }

    @Override
    public OssDeleteObjectResponseModel deleteObject(OssKeyRequestModel command) {
        validateKey(command);
        long startedAt = System.nanoTime();
        ObjectState state = state(command.bucket(), command.objectKey());
        boolean existed = state.exists();
        try {
            Files.deleteIfExists(state.path());
            return new OssDeleteObjectResponseModel(
                    true,
                    providerCode(),
                    storageType(),
                    state.bucket(),
                    command.objectKey(),
                    existed,
                    elapsedMillis(startedAt)
            );
        } catch (IOException ex) {
            throw storageFailed("Local storage deleteObject failed", ex);
        }
    }

    @Override
    public OssCopyObjectResponseModel copyObject(OssCopyObjectRequestModel command) {
        validateCopy(command);
        long startedAt = System.nanoTime();
        String sourceBucket = bucket(command.sourceBucket());
        String targetBucket = bucket(command.targetBucket());
        Path source = resolve(sourceBucket, command.sourceObjectKey());
        Path target = resolve(targetBucket, command.targetObjectKey());
        if (!Files.exists(source)) {
            throw notFound(sourceBucket, command.sourceObjectKey());
        }
        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            byte[] content = Files.readAllBytes(target);
            String etag = sha256Hex(content);
            return new OssCopyObjectResponseModel(
                    true,
                    providerCode(),
                    storageType(),
                    sourceBucket,
                    command.sourceObjectKey(),
                    targetBucket,
                    command.targetObjectKey(),
                    etag,
                    (long) content.length,
                    elapsedMillis(startedAt)
            );
        } catch (IOException ex) {
            throw storageFailed("Local storage copyObject failed", ex);
        }
    }

    @Override
    public OssHealthResponseModel healthCheck(OssHealthRequestModel command) {
        long startedAt = System.nanoTime();
        String bucket = bucket(command == null ? null : command.bucket());
        boolean writeTest = command != null && Boolean.TRUE.equals(command.writeTestEnabled());
        Map<String, Object> providerResult = new LinkedHashMap<>();
        try {
            Files.createDirectories(bucketRoot(bucket));
            boolean writePassed = false;
            if (writeTest) {
                String prefix = StringUtils.hasText(command.testPrefix()) ? command.testPrefix() : "health-check";
                String key = trimSlashes(prefix) + "/storage-health-" + System.currentTimeMillis() + ".txt";
                OssPutObjectRequestModel put = new OssPutObjectRequestModel(
                        providerCode(),
                        "health-check",
                        "health-check",
                        bucket,
                        key,
                        "text/plain",
                        Base64.getEncoder().encodeToString("ok".getBytes(StandardCharsets.UTF_8)),
                        2L,
                        null,
                        Map.of("source", "health-check"),
                        Map.of()
                );
                putObject(put);
                deleteObject(new OssKeyRequestModel(providerCode(), "health-check", "health-check", bucket, key));
                writePassed = true;
            }
            providerResult.put("root", bucketRoot(bucket).toAbsolutePath().toString());
            return new OssHealthResponseModel(
                    true,
                    providerCode(),
                    storageType(),
                    bucket,
                    true,
                    writePassed,
                    true,
                    elapsedMillis(startedAt),
                    null,
                    null,
                    providerResult
            );
        } catch (Exception ex) {
            providerResult.put("exception", ex.getClass().getSimpleName());
            return new OssHealthResponseModel(
                    false,
                    providerCode(),
                    storageType(),
                    bucket,
                    false,
                    false,
                    true,
                    elapsedMillis(startedAt),
                    "FILE_STORAGE_FAILED",
                    "Local storage health check failed",
                    providerResult
            );
        }
    }

    private OssGetObjectResponseModel result(
            OssKeyRequestModel command,
            ObjectState state,
            String contentBase64,
            long latencyMs
    ) {
        try {
            byte[] content = Files.readAllBytes(state.path());
            String sha256 = sha256Hex(content);
            return new OssGetObjectResponseModel(
                    true,
                    true,
                    providerCode(),
                    storageType(),
                    state.bucket(),
                    command.objectKey(),
                    contentBase64,
                    (long) content.length,
                    sha256,
                    sha256,
                    contentType(null),
                    lastModified(state.path()),
                    Map.of("sha256", sha256),
                    latencyMs
            );
        } catch (IOException ex) {
            throw storageFailed("Local storage metadata read failed", ex);
        }
    }

    private Path bucketRoot(String bucket) {
        return Path.of(localRoot).toAbsolutePath().normalize().resolve(bucket).normalize();
    }

    private Path resolve(String bucket, String objectKey) {
        Path root = bucketRoot(bucket);
        Path path = root.resolve(trimSlashes(objectKey)).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("objectKey is invalid");
        }
        return path;
    }

    private ObjectState state(String requestedBucket, String objectKey) {
        String bucket = bucket(requestedBucket);
        Path path = resolve(bucket, objectKey);
        return new ObjectState(bucket, path, Files.exists(path));
    }

    private String bucket(String requestedBucket) {
        return StringUtils.hasText(requestedBucket) ? requestedBucket : defaultBucket;
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

    private static Map<String, String> metadata(
            Map<String, String> source,
            String tenantId,
            String fileId,
            String sha256
    ) {
        Map<String, String> metadata = new LinkedHashMap<>();
        if (source != null) {
            metadata.putAll(source);
        }
        metadata.put("tenant-id", tenantId);
        metadata.put("file-id", fileId);
        metadata.put("sha256", sha256);
        return metadata;
    }

    private static OffsetDateTime lastModified(Path path) throws IOException {
        Instant instant = Files.getLastModifiedTime(path).toInstant();
        return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private static String contentType(String contentType) {
        return StringUtils.hasText(contentType) ? contentType : "application/octet-stream";
    }

    private String localUrl(String bucket, String objectKey, OffsetDateTime expiresAt) {
        String encodedKey = URLEncoder.encode(objectKey, StandardCharsets.UTF_8);
        String baseUrl = publicBaseUrl;
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "local-storage://" + bucket;
        }
        return baseUrl.replaceAll("/+$", "") + "/" + encodedKey + "?expiresAt=" + expiresAt.toInstant().toEpochMilli();
    }

    private static String trimSlashes(String value) {
        return value == null ? "" : value.replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private static String sha256Hex(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to calculate SHA-256 digest", ex);
        }
    }

    private static IllegalStateException storageFailed(String message, Exception ex) {
        return new IllegalStateException(message, ex);
    }

    private static IllegalStateException notFound(String bucket, String objectKey) {
        return new IllegalStateException("Storage object not found: bucket=" + bucket + ", objectKey=" + objectKey);
    }

    private static long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }

    private record ObjectState(String bucket, Path path, boolean exists) {
    }
}
