package cloud.techotakus.invoice.integration.domain.usecase;

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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OssIntegrationUseCase {

    private final String defaultProvider;
    private final Map<String, OssIntegrationRepository> repositories;

    public OssIntegrationUseCase(
            @Value("${invoice.integration.storage.provider:LOCAL}") String defaultProvider,
            List<OssIntegrationRepository> repositories
    ) {
        this.defaultProvider = defaultProvider;
        this.repositories = repositories.stream()
                .collect(Collectors.toUnmodifiableMap(
                        repository -> normalize(repository.providerCode()),
                        Function.identity()
                ));
    }

    public OssPutObjectResponseModel putObject(OssPutObjectRequestModel command) {
        return resolveProvider(command == null ? null : command.providerCode()).putObject(command);
    }

    public OssGetObjectResponseModel getObject(OssKeyRequestModel command) {
        return resolveProvider(command == null ? null : command.providerCode()).getObject(command);
    }

    public OssPresignedUrlResponseModel presignGetObject(OssPresignObjectRequestModel command) {
        return resolveProvider(command == null ? null : command.providerCode()).presignGetObject(command);
    }

    public OssGetObjectResponseModel getObjectMetadata(OssKeyRequestModel command) {
        return resolveProvider(command == null ? null : command.providerCode()).getObjectMetadata(command);
    }

    public OssGetObjectResponseModel objectExists(OssKeyRequestModel command) {
        return resolveProvider(command == null ? null : command.providerCode()).objectExists(command);
    }

    public OssDeleteObjectResponseModel deleteObject(OssKeyRequestModel command) {
        return resolveProvider(command == null ? null : command.providerCode()).deleteObject(command);
    }

    public OssCopyObjectResponseModel copyObject(OssCopyObjectRequestModel command) {
        return resolveProvider(command == null ? null : command.providerCode()).copyObject(command);
    }

    public OssHealthResponseModel healthCheck(OssHealthRequestModel command) {
        return resolveProvider(command == null ? null : command.providerCode()).healthCheck(command);
    }

    private OssIntegrationRepository resolveProvider(String requestedProvider) {
        String providerCode = StringUtils.hasText(requestedProvider) ? requestedProvider : defaultProvider;
        OssIntegrationRepository repository = repositories.get(normalize(providerCode));
        if (repository == null) {
            throw new IllegalStateException("Storage provider is not available: " + providerCode);
        }
        return repository;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
