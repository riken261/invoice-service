package cloud.techotakus.invoice.ocr.domain.usecase;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.ocr.domain.entity.OcrRecognizeRequestEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrRecognizeResponseEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrVatVerificationRequestEntity;
import cloud.techotakus.invoice.ocr.domain.entity.OcrVatVerificationResponseEntity;
import cloud.techotakus.invoice.ocr.domain.repository.OcrOperationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OcrOperationUseCaseTest {

    @Test
    void emptyProviderItemsRequireManualInputWithZeroConfidence() {
        FakeRepository repository = new FakeRepository();
        repository.recognizeResponse = success(rawResponse(List.of()));
        OcrOperationUseCase useCase = useCase(repository);

        OcrRecognizeResponseEntity response = useCase.recognize(request());

        assertThat(response.getInvoiceItems()).hasSize(1);
        Map<String, Object> item = response.getInvoiceItems().getFirst();
        assertThat(item.get("confidence")).isEqualTo(0.0d);
        assertThat(item.get("lowConfidence")).isEqualTo(true);
        assertThat(item.get("manualInputRequired")).isEqualTo(true);
        assertThat(repository.verifyCalled).isFalse();
    }

    @Test
    void missingInvoiceNumberHasZeroConfidenceAndSkipsVatVerify() {
        FakeRepository repository = new FakeRepository();
        repository.recognizeResponse = success(rawResponse(List.of(vatItem(Map.of(
                "Date", "2026-07-02",
                "Total", "128.00"
        )))));
        OcrOperationUseCase useCase = useCase(repository);

        OcrRecognizeResponseEntity response = useCase.recognize(request());

        Map<String, Object> item = response.getInvoiceItems().getFirst();
        assertThat(item.get("confidence")).isEqualTo(0.0d);
        assertThat(item.get("lowConfidence")).isEqualTo(true);
        assertThat(item.get("manualInputRequired")).isEqualTo(true);
        assertThat(repository.verifyCalled).isFalse();
    }

    @Test
    void failedVatVerifyLowersConfidenceBelowThreshold() {
        FakeRepository repository = new FakeRepository();
        repository.recognizeResponse = success(rawResponse(List.of(vatItem(Map.of(
                "Number", "26327000001155914823",
                "Date", "2026-07-02",
                "Total", "128.00"
        )))));
        repository.verifyResponse = verification(false, -0.30d);
        OcrOperationUseCase useCase = useCase(repository);

        OcrRecognizeResponseEntity response = useCase.recognize(request());

        Map<String, Object> item = response.getInvoiceItems().getFirst();
        assertThat(repository.verifyCalled).isTrue();
        assertThat((Double) item.get("confidence")).isCloseTo(0.50d, org.assertj.core.data.Offset.offset(0.0001d));
        assertThat(item.get("lowConfidence")).isEqualTo(true);
        assertThat(item.get("vatMatched")).isEqualTo(false);
    }

    @Test
    void vatVerifyRequestUsesTencentDateAndAmountFormat() {
        FakeRepository repository = new FakeRepository();
        repository.recognizeResponse = success(rawResponse(List.of(vatItem(Map.of(
                "Number", "26327000001155914823",
                "Date", "2026\u5e7406\u670820\u65e5",
                "Total", "\uffe56.00"
        )))));
        repository.verifyResponse = verification(true, 0.10d);
        OcrOperationUseCase useCase = useCase(repository);

        useCase.recognize(request());

        assertThat(repository.verifyRequest.getInvoiceDate()).isEqualTo("2026-06-20");
        assertThat(repository.verifyRequest.getAmount()).isEqualTo("6.00");
        assertThat(repository.verifyRequest.getInvoiceCode()).isNull();
        assertThat(repository.verifyRequest.getInvoiceKind()).isEqualTo("10");
        assertThat(repository.verifyRequest.getEnableCommonElectronic()).isNull();
        assertThat(repository.verifyRequest.getSellerTaxCode()).isNull();
    }

    @Test
    void paperVatVerifyRequestUsesCodeCheckCodeAndAmountWithoutTax() {
        FakeRepository repository = new FakeRepository();
        repository.recognizeResponse = success(rawResponse(List.of(vatItem(Map.of(
                "Code", "144002288010",
                "Number", "04138864",
                "Date", "2026-06-20",
                "CheckCode", "12345678901234567890",
                "PretaxAmount", "88.88",
                "Total", "99.99"
        )))));
        repository.verifyResponse = verification(true, 0.10d);
        OcrOperationUseCase useCase = useCase(repository);

        useCase.recognize(request());

        assertThat(repository.verifyRequest.getInvoiceNo()).isEqualTo("04138864");
        assertThat(repository.verifyRequest.getInvoiceCode()).isEqualTo("144002288010");
        assertThat(repository.verifyRequest.getCheckCode()).isEqualTo("567890");
        assertThat(repository.verifyRequest.getAmount()).isEqualTo("88.88");
        assertThat(repository.verifyRequest.getInvoiceKind()).isNull();
        assertThat(repository.verifyRequest.getEnableCommonElectronic()).isNull();
    }

    @Test
    void verifiesEveryVatInvoiceItem() {
        FakeRepository repository = new FakeRepository();
        repository.recognizeResponse = success(rawResponse(List.of(
                vatItem(Map.of(
                        "Number", "26327000001155914823",
                        "Date", "2026-07-02",
                        "Total", "128.00"
                )),
                vatItem(Map.of(
                        "Number", "26327000001155914824",
                        "Date", "2026-07-02",
                        "Total", "256.00"
                ))
        )));
        repository.verifyResponse = verification(false, -0.30d);
        OcrOperationUseCase useCase = useCase(repository);

        OcrRecognizeResponseEntity response = useCase.recognize(request());

        assertThat(repository.verifyCallCount).isEqualTo(2);
        assertThat(response.getInvoiceItems())
                .allSatisfy(item -> assertThat(item.get("lowConfidence")).isEqualTo(true));
    }

    @Test
    void vatVerifyExceptionLowersConfidenceInsteadOfFailingRecognition() {
        FakeRepository repository = new FakeRepository();
        repository.recognizeResponse = success(rawResponse(List.of(vatItem(Map.of(
                "Number", "26327000001155914823",
                "Date", "2026-07-02",
                "Total", "128.00"
        )))));
        repository.verifyException = new ServiceException("verify unavailable", ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE);
        OcrOperationUseCase useCase = useCase(repository);

        OcrRecognizeResponseEntity response = useCase.recognize(request());

        Map<String, Object> item = response.getInvoiceItems().getFirst();
        assertThat(repository.verifyCalled).isTrue();
        assertThat((Double) item.get("confidence")).isCloseTo(0.50d, org.assertj.core.data.Offset.offset(0.0001d));
        assertThat(item.get("lowConfidence")).isEqualTo(true);
        assertThat(response.getVatVerification().getMatched()).isFalse();
    }

    private static OcrOperationUseCase useCase(FakeRepository repository) {
        OcrOperationUseCase useCase = new OcrOperationUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);
        ReflectionTestUtils.setField(useCase, "vatVerifyEnabled", true);
        ReflectionTestUtils.setField(useCase, "baseConfidence", 0.85d);
        ReflectionTestUtils.setField(useCase, "lowConfidenceThreshold", 0.70d);
        ReflectionTestUtils.setField(useCase, "vatVerifyFailedConfidenceDelta", -0.30d);
        return useCase;
    }

    private static OcrRecognizeRequestEntity request() {
        OcrRecognizeRequestEntity request = new OcrRecognizeRequestEntity();
        request.setTenantId("ichigo");
        request.setFileBase64("base64");
        return request;
    }

    private static OcrRecognizeResponseEntity success(Map<String, Object> rawResult) {
        OcrRecognizeResponseEntity response = new OcrRecognizeResponseEntity();
        response.setSuccess(true);
        response.setProviderCode("TENCENT_CLOUD");
        response.setRawResult(rawResult);
        return response;
    }

    private static OcrVatVerificationResponseEntity verification(boolean matched, double delta) {
        OcrVatVerificationResponseEntity response = new OcrVatVerificationResponseEntity();
        response.setVerified(matched);
        response.setMatched(matched);
        response.setConfidenceBoosted(matched);
        response.setConfidenceDelta(delta);
        return response;
    }

    private static Map<String, Object> rawResponse(List<Map<String, Object>> items) {
        return new LinkedHashMap<>(Map.of(
                "Response", new LinkedHashMap<>(Map.of(
                        "RequestId", "req_1",
                        "MixedInvoiceItems", items
                ))
        ));
    }

    private static Map<String, Object> vatItem(Map<String, Object> invoiceFields) {
        return new LinkedHashMap<>(Map.of(
                "Code", "OK",
                "Type", 3,
                "SingleInvoiceInfos", new LinkedHashMap<>(Map.of("VatElectronicCommonInvoice", invoiceFields))
        ));
    }

    private static final class FakeRepository implements OcrOperationRepository {
        private OcrRecognizeResponseEntity recognizeResponse;
        private OcrVatVerificationResponseEntity verifyResponse;
        private RuntimeException verifyException;
        private boolean verifyCalled;
        private int verifyCallCount;
        private OcrVatVerificationRequestEntity verifyRequest;

        @Override
        public OcrRecognizeResponseEntity recognize(OcrRecognizeRequestEntity request) {
            return recognizeResponse;
        }

        @Override
        public OcrVatVerificationResponseEntity verify(OcrVatVerificationRequestEntity request) {
            verifyCalled = true;
            verifyCallCount++;
            verifyRequest = request;
            if (verifyException != null) {
                throw verifyException;
            }
            return verifyResponse;
        }
    }
}
