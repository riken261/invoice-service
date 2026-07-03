package cloud.techotakus.invoice.integration.app.mapstruct;

import cloud.techotakus.invoice.integration.api.model.OcrRecognizeRequestModel;
import cloud.techotakus.invoice.integration.api.model.OcrRecognizeResponseModel;
import cloud.techotakus.invoice.integration.api.model.OcrVatVerificationRequestModel;
import cloud.techotakus.invoice.integration.api.model.OcrVatVerificationResponseModel;
import cloud.techotakus.invoice.integration.domain.entity.OcrProviderRequestEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrProviderResponseEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrVatVerificationRequestEntity;
import cloud.techotakus.invoice.integration.domain.entity.OcrVatVerificationResponseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OcrIntegrationMapstruct {

    OcrProviderRequestEntity map(OcrRecognizeRequestModel model);

    OcrRecognizeResponseModel map(OcrProviderResponseEntity entity);

    OcrVatVerificationRequestEntity map(OcrVatVerificationRequestModel model);

    OcrVatVerificationResponseModel map(OcrVatVerificationResponseEntity entity);
}
