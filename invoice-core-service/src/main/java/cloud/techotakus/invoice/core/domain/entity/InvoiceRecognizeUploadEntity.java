package cloud.techotakus.invoice.core.domain.entity;

import lombok.Data;

import java.util.List;

@Data
public class InvoiceRecognizeUploadEntity {
    private String batchId;
    private List<InvoiceRecognizeSessionEntity> sessions;
}
