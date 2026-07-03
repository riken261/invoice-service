package cloud.techotakus.invoice.core.domain.repository;

import cloud.techotakus.invoice.core.domain.entity.InvoiceEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceFileAccessEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeSessionEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeUploadFileEntity;

import java.util.List;

public interface InvoiceRecognizeRepository {

    void saveSession(InvoiceRecognizeSessionEntity session);

    void updateSession(InvoiceRecognizeSessionEntity session);

    InvoiceRecognizeSessionEntity findSession(String sessionId);

    List<InvoiceRecognizeSessionEntity> findSessionsByBatchId(String batchId);

    InvoiceEntity findInvoice(String invoiceId);

    InvoiceEntity findInvoiceByFileId(String fileId);

    void saveInvoice(InvoiceEntity invoice);

    void updateInvoice(InvoiceEntity invoice);

    String uploadFile(InvoiceRecognizeSessionEntity session, InvoiceRecognizeUploadFileEntity file);

    InvoiceFileAccessEntity previewFile(String fileId);

    InvoiceRecognizeSessionEntity recognize(InvoiceRecognizeSessionEntity session, InvoiceRecognizeUploadFileEntity file);
}
