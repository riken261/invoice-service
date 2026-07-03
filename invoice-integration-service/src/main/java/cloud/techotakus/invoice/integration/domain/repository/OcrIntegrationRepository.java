package cloud.techotakus.invoice.integration.domain.repository;

import cloud.techotakus.invoice.integration.domain.entity.OcrProviderRequestEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrProviderResponseEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrVatVerificationRequestEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrVatVerificationResponseEntity;

public interface OcrIntegrationRepository {

    String providerCode();

    OcrProviderResponseEntity recognize(OcrProviderRequestEntity request);

    OcrVatVerificationResponseEntity verify(OcrVatVerificationRequestEntity request);

}
