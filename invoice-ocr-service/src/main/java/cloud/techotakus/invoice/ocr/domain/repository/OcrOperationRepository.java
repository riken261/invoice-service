package cloud.techotakus.invoice.ocr.domain.repository;

import cloud.techotakus.invoice.ocr.domain.entity.OcrRecognizeRequestEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrRecognizeResponseEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrVatVerificationRequestEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrVatVerificationResponseEntity;

public interface OcrOperationRepository {

    OcrRecognizeResponseEntity recognize(OcrRecognizeRequestEntity request);

    OcrVatVerificationResponseEntity verify(OcrVatVerificationRequestEntity request);
}
