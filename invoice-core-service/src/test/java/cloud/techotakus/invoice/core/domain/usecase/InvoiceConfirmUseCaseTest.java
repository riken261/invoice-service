package cloud.techotakus.invoice.core.domain.usecase;

import cloud.techotakus.invoice.core.domain.entity.InvoiceEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceConfirmEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceFileAccessEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceManualInputSubmitEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeSessionEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeUploadFileEntity;
import cloud.techotakus.invoice.core.domain.repository.InvoiceNonceRepository;
import cloud.techotakus.invoice.core.domain.repository.InvoiceRecognizeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceConfirmUseCaseTest {

    @Test
    void submitManualInputUpdatesInvoiceAndVerifiesNonceAgainstInvoice() {
        FakeRepository repository = new FakeRepository();
        repository.invoice = invoice();
        FakeNonceRepository nonceRepository = new FakeNonceRepository();
        InvoiceConfirmUseCase useCase = new InvoiceConfirmUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);
        ReflectionTestUtils.setField(useCase, "nonceRepository", nonceRepository);

        InvoiceEntity result = useCase.submitManualInput(request());

        assertThat(nonceRepository.operation).isEqualTo("invoice.manual-input");
        assertThat(nonceRepository.resourceId).isEqualTo("inv_1");
        assertThat(repository.updatedInvoice).isSameAs(result);
        assertThat(result.getManualInput()).isTrue();
        assertThat(result.getInvoiceStatus()).isEqualTo("USER_CONFIRMED");
        assertThat(result.getManualFields()).containsEntry("invoiceNumber", "NO-1");
        assertThat(result.getConfirmedFields()).containsEntry("invoiceNumber", "NO-1");
        assertThat(result.getInvoiceNumber()).isEqualTo("NO-1");
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("128.50"));
        assertThat(result.getUpdatedTrace()).isEqualTo("manual-key");
    }

    @Test
    void submitSessionManualInputCreatesInvoiceAndVerifiesNonceAgainstRecognitionSession() {
        FakeRepository repository = new FakeRepository();
        repository.session = session();
        FakeNonceRepository nonceRepository = new FakeNonceRepository();
        InvoiceConfirmUseCase useCase = new InvoiceConfirmUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);
        ReflectionTestUtils.setField(useCase, "nonceRepository", nonceRepository);

        InvoiceManualInputSubmitEntity request = request();
        request.setInvoiceId(null);
        request.setRecognitionSessionId("ocr_session_1");
        InvoiceEntity result = useCase.submitSessionManualInput(request);

        assertThat(nonceRepository.operation).isEqualTo("invoice.manual-input");
        assertThat(nonceRepository.resourceId).isEqualTo("ocr_session_1");
        assertThat(repository.savedInvoice).isSameAs(result);
        assertThat(repository.updatedSession.getSubmittedInvoiceId()).isEqualTo(result.getId());
        assertThat(result.getInvoiceFileId()).isEqualTo("file_1");
        assertThat(result.getManualInput()).isTrue();
        assertThat(result.getManualFields()).containsEntry("invoiceNumber", "NO-1");
    }

    @Test
    void confirmSessionReturnsSubmittedInvoiceWhenSessionWasManuallySubmitted() {
        FakeRepository repository = new FakeRepository();
        repository.invoice = invoice();
        repository.session = session();
        repository.session.setStatus("SUBMITTED");
        repository.session.setSubmittedInvoiceId("inv_1");
        FakeNonceRepository nonceRepository = new FakeNonceRepository();
        InvoiceConfirmUseCase useCase = new InvoiceConfirmUseCase();
        ReflectionTestUtils.setField(useCase, "repository", repository);
        ReflectionTestUtils.setField(useCase, "nonceRepository", nonceRepository);

        InvoiceConfirmEntity request = confirmRequest();
        InvoiceEntity result = useCase.confirm(request);

        assertThat(nonceRepository.operation).isEqualTo("invoice.confirm");
        assertThat(nonceRepository.resourceId).isEqualTo("ocr_session_1");
        assertThat(result).isSameAs(repository.invoice);
        assertThat(repository.savedInvoice).isNull();
    }

    private static InvoiceConfirmEntity confirmRequest() {
        InvoiceConfirmEntity request = new InvoiceConfirmEntity();
        request.setTenantId("tenant_1");
        request.setOwnerUserId("user_1");
        request.setSessionId("session_1");
        request.setRecognitionSessionId("ocr_session_1");
        request.setNonce("nonce_1");
        request.setConfirmedFields(Map.of(
                "invoiceNumber", "NO-1",
                "totalAmount", "128.50"
        ));
        return request;
    }

    private static InvoiceManualInputSubmitEntity request() {
        InvoiceManualInputSubmitEntity request = new InvoiceManualInputSubmitEntity();
        request.setTenantId("tenant_1");
        request.setOwnerUserId("user_1");
        request.setSessionId("session_1");
        request.setInvoiceId("inv_1");
        request.setNonce("nonce_1");
        request.setIdempotencyKey("manual-key");
        request.setFields(Map.of(
                "invoiceNumber", "NO-1",
                "totalAmount", "128.50"
        ));
        return request;
    }

    private static InvoiceEntity invoice() {
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setId("inv_1");
        invoice.setTenantId("tenant_1");
        invoice.setOwnerUserId("user_1");
        return invoice;
    }

    private static final class FakeNonceRepository implements InvoiceNonceRepository {
        private String operation;
        private String resourceId;

        @Override
        public void verifyAndConsume(String tenantId, String ownerUserId, String sessionId, String operation, String nonce) {
        }

        @Override
        public void verifyAndConsume(
                String tenantId,
                String ownerUserId,
                String sessionId,
                String operation,
                String resourceId,
                String nonce
        ) {
            this.operation = operation;
            this.resourceId = resourceId;
        }
    }

    private static final class FakeRepository implements InvoiceRecognizeRepository {
        private InvoiceEntity invoice;
        private InvoiceEntity savedInvoice;
        private InvoiceEntity updatedInvoice;
        private InvoiceRecognizeSessionEntity session;
        private InvoiceRecognizeSessionEntity updatedSession;

        @Override
        public void saveSession(InvoiceRecognizeSessionEntity session) {
        }

        @Override
        public void updateSession(InvoiceRecognizeSessionEntity session) {
            this.updatedSession = session;
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
            return invoice;
        }

        @Override
        public InvoiceEntity findInvoiceByFileId(String fileId) {
            return null;
        }

        @Override
        public void saveInvoice(InvoiceEntity invoice) {
            this.savedInvoice = invoice;
        }

        @Override
        public void updateInvoice(InvoiceEntity invoice) {
            this.updatedInvoice = invoice;
        }

        @Override
        public String uploadFile(InvoiceRecognizeSessionEntity session, InvoiceRecognizeUploadFileEntity file) {
            return null;
        }

        @Override
        public InvoiceFileAccessEntity previewFile(String fileId) {
            return null;
        }

        @Override
        public InvoiceRecognizeSessionEntity recognize(
                InvoiceRecognizeSessionEntity session,
                InvoiceRecognizeUploadFileEntity file
        ) {
            return null;
        }
    }

    private static InvoiceRecognizeSessionEntity session() {
        InvoiceRecognizeSessionEntity session = new InvoiceRecognizeSessionEntity();
        session.setId("ocr_session_1");
        session.setTenantId("tenant_1");
        session.setOwnerUserId("user_1");
        session.setInvoiceFileId("file_1");
        session.setStatus("LOW_CONFIDENCE");
        session.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        return session;
    }
}
