package cloud.techotakus.invoice.ocr.app.mapstruct;

import cloud.techotakus.invoice.ocr.api.model.OcrRecognizeRequestModel;
import cloud.techotakus.invoice.ocr.api.model.OcrRecognizeResponseModel;
import cloud.techotakus.invoice.ocr.api.model.OcrVatVerificationRequestModel;
import cloud.techotakus.invoice.ocr.api.model.OcrVatVerificationResponseModel;
import cloud.techotakus.invoice.ocr.domain.entity.OcrRecognizeRequestEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrRecognizeResponseEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrVatVerificationRequestEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrVatVerificationResponseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OcrOperationMapstruct {

    OcrRecognizeRequestEntity map(OcrRecognizeRequestModel model);

    OcrRecognizeResponseModel map(OcrRecognizeResponseEntity entity);

    OcrVatVerificationRequestEntity map(OcrVatVerificationRequestModel model);

    OcrVatVerificationResponseModel map(OcrVatVerificationResponseEntity entity);
}
