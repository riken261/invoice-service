package cloud.techotakus.invoice.core.domain.usecase;

import cloud.techotakus.invoice.core.domain.entity.InvoiceEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceFileAccessEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeSessionEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeUploadFileEntity;
import cloud.techotakus.invoice.core.domain.repository.InvoiceRecognizeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceRecognizeUseCaseTest {

    @Test
    void emptyOcrItemsResolveToLowConfidence() {
        InvoiceRecognizeUseCase useCase = new InvoiceRecognizeUseCase();
        InvoiceRecognizeSessionEntity session = new InvoiceRecognizeSessionEntity();
        session.setNormalizedResult(Map.of("success", true, "invoiceItems", List.of()));

        String status = ReflectionTestUtils.invokeMethod(useCase, "resolveSuccessStatus", session);

        assertThat(status).isEqualTo("LOW_CONFIDENCE");
    }

    @Test
    void failedOcrResponseResolvesToFailed() {
        InvoiceRecognizeUseCase useCase = new InvoiceRecognizeUseCase();
        InvoiceRecognizeSessionEntity session = new InvoiceRecognizeSessionEntity();
        session.setNormalizedResult(Map.of("success", false));

        String status = ReflectionTestUtils.invokeMethod(useCase, "resolveSuccessStatus", session);

        assertThat(status).isEqualTo("OCR_FAILED");
    }

    @Test
    void anyLowConfidenceItemResolvesToLowConfidence() {
        InvoiceRecognizeUseCase useCase = new InvoiceRecognizeUseCase();
        InvoiceRecognizeSessionEntity session = new InvoiceRecognizeSessionEntity();
        session.setNormalizedResult(Map.of(
                "success", true,
                "invoiceItems", List.of(Map.of("lowConfidence", true))
        ));

        String status = ReflectionTestUtils.invokeMethod(useCase, "resolveSuccessStatus", session);

        assertThat(status).isEqualTo("LOW_CONFIDENCE");
    }

    @Test
    void previewSessionUsesOwnedRecognitionSessionFile() {
        FakeRepository repository = new FakeRepository();
        repository.session = session();
        repository.preview = preview();
        InvoiceRecognizeUseCase useCase = new InvoiceRecognizeUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);

        InvoiceFileAccessEntity result = useCase.previewSession("tenant_1", "user_1", "ocr_session_1");

        assertThat(repository.previewFileId).isEqualTo("file_1");
        assertThat(result).isSameAs(repository.preview);
    }

    private static InvoiceRecognizeSessionEntity session() {
        InvoiceRecognizeSessionEntity session = new InvoiceRecognizeSessionEntity();
        session.setId("ocr_session_1");
        session.setTenantId("tenant_1");
        session.setOwnerUserId("user_1");
        session.setInvoiceFileId("file_1");
        return session;
    }

    private static InvoiceFileAccessEntity preview() {
        InvoiceFileAccessEntity entity = new InvoiceFileAccessEntity();
        entity.setFileId("file_1");
        entity.setUrl("https://example.test/file_1");
        entity.setMethod("GET");
        return entity;
    }

    private static final class FakeRepository implements InvoiceRecognizeRepository {
        private InvoiceRecognizeSessionEntity session;
        private InvoiceFileAccessEntity preview;
        private String previewFileId;

        @Override
        public void saveSession(InvoiceRecognizeSessionEntity session) {
        }

        @Override
        public void updateSession(InvoiceRecognizeSessionEntity session) {
        }

        @Override
        public InvoiceRecognizeSessionEntity findSession(String sessionId) {
            return session;
        }

        @Override
        public List<InvoiceRecognizeSessionEntity> findSessionsByBatchId(String batchId) {
            return List.of();
        }

        @Override
        public InvoiceEntity findInvoice(String invoiceId) {
            return null;
        }

        @Override
        public InvoiceEntity findInvoiceByFileId(String fileId) {
            return null;
        }

        @Override
        public void saveInvoice(InvoiceEntity invoice) {
        }

        @Override
        public void updateInvoice(InvoiceEntity invoice) {
        }

        @Override
        public String uploadFile(InvoiceRecognizeSessionEntity session, InvoiceRecognizeUploadFileEntity file) {
            return null;
        }

        @Override
        public InvoiceFileAccessEntity previewFile(String fileId) {
            previewFileId = fileId;
            return preview;
        }

        @Override
        public InvoiceRecognizeSessionEntity recognize(
                InvoiceRecognizeSessionEntity session,
                InvoiceRecognizeUploadFileEntity file
        ) {
            return null;
        }
    }
}
