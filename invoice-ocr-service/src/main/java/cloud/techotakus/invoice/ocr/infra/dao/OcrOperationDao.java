package cloud.techotakus.invoice.ocr.infra.dao;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.ocr.domain.entity.OcrRecognizeRequestEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrRecognizeResponseEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrVatVerificationRequestEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrVatVerificationResponseEntity;
import cloud.techotakus.invoice.ocr.domain.repository.OcrOperationRepository;
import cloud.techotakus.invoice.ocr.infra.client.OcrIntegrationClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class OcrOperationDao implements OcrOperationRepository {

    @Resource
    private OcrIntegrationClient integrationClient;

    @Override
    public OcrRecognizeResponseEntity recognize(OcrRecognizeRequestEntity request) {
        return unwrap(integrationClient.recognize(request), "Integration OCR recognize failed");
    }

    @Override
    public OcrVatVerificationResponseEntity verify(OcrVatVerificationRequestEntity request) {
        return unwrap(integrationClient.verify(request), "Integration OCR VAT verification failed");
    }

    private static <T> T unwrap(RestResponse<T> response, String defaultMessage) {
        if (response != null && response.isSuccess()) {
            return response.getData();
        }
        String message = response == null || !StringUtils.hasText(response.getError())
                ? defaultMessage
                : response.getError();
        throw new ServiceException(message, ErrorCode.OCR_PROVIDER_FAILED);
    }
}
