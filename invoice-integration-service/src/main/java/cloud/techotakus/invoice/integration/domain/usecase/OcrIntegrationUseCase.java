package cloud.techotakus.invoice.integration.domain.usecase;

import cloud.techotakus.invoice.integration.domain.entity.OcrProviderRequestEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrProviderResponseEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrVatVerificationRequestEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrVatVerificationResponseEntity;
import cloud.techotakus.invoice.integration.domain.repository.OcrIntegrationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OcrIntegrationUseCase {

    private final String defaultProvider;
    private final Map<String, OcrIntegrationRepository> repositories;

    public OcrIntegrationUseCase(
            @Value("${invoice.integration.ocr.provider:MOCK}") String defaultProvider,
            List<OcrIntegrationRepository> repositories
    ) {
        this.defaultProvider = defaultProvider;
        this.repositories = repositories.stream()
                .collect(Collectors.toUnmodifiableMap(
                        repository -> normalize(repository.providerCode()),
                        Function.identity()
                ));
    }

    public OcrProviderResponseEntity recognize(OcrProviderRequestEntity request) {
        return resolveProvider(request == null ? null : request.getProviderCode()).recognize(request);
    }

    public OcrVatVerificationResponseEntity verify(OcrVatVerificationRequestEntity request) {
        return resolveProvider(request == null ? null : request.getProviderCode()).verify(request);
    }

    private OcrIntegrationRepository resolveProvider(String requestedProvider) {
        String providerCode = StringUtils.hasText(requestedProvider) ? requestedProvider : defaultProvider;
        OcrIntegrationRepository repository = repositories.get(normalize(providerCode));
        if (repository == null) {
            throw new IllegalStateException("OCR provider is not available: " + providerCode);
        }
        return repository;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
