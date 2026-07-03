package cloud.techotakus.invoice.core.app.mapstruct;

import cloud.techotakus.invoice.core.api.model.InvoiceConfirmResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoicePreviewResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoiceRecognizeBatchResultResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoiceRecognizeResultResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoiceRecognizeSessionSummaryModel;
import cloud.techotakus.invoice.core.api.model.InvoiceSummaryModel;
import cloud.techotakus.invoice.core.domain.entity.InvoiceEntity;
import cloud.techotakus.invoice.core.api.model.InvoiceRecognizeUploadFileModel;
import cloud.techotakus.invoice.core.api.model.InvoiceRecognizeUploadRequestModel;
import cloud.techotakus.invoice.core.api.model.InvoiceRecognizeUploadResponseModel;
import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeSessionEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeUploadFileEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceFileAccessEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvoiceRecognizeMapstruct {

    @Mapping(target = "batchId", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "ownerUserId", ignore = true)
    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoiceFileId", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "rawRequestId", ignore = true)
    @Mapping(target = "rawResult", ignore = true)
    @Mapping(target = "normalizedResult", ignore = true)
    @Mapping(target = "errorCode", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "finishedAt", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "submittedInvoiceId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdTrace", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedTrace", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    InvoiceRecognizeSessionEntity map(InvoiceRecognizeUploadRequestModel model);

    @Mapping(target = "fileId", ignore = true)
    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "status", ignore = true)
    InvoiceRecognizeUploadFileEntity map(InvoiceRecognizeUploadFileModel model);

    List<InvoiceRecognizeUploadFileEntity> mapFiles(List<InvoiceRecognizeUploadFileModel> models);

    @Mapping(target = "sessionId", source = "id")
    @Mapping(target = "fileId", source = "invoiceFileId")
    InvoiceRecognizeResultResponseModel mapResult(InvoiceRecognizeSessionEntity entity);

    default InvoiceRecognizeBatchResultResponseModel mapBatchResult(
            String batchId,
            List<InvoiceRecognizeSessionEntity> sessions,
            long nextPollAfterMs
    ) {
        boolean completed = sessions == null || sessions.stream().noneMatch(this::isPending);
        return new InvoiceRecognizeBatchResultResponseModel(
                batchId,
                completed ? "COMPLETED" : "PROCESSING",
                completed,
                completed ? null : nextPollAfterMs,
                sessions == null ? List.of() : sessions.stream().map(this::mapResult).toList()
        );
    }

    private boolean isPending(InvoiceRecognizeSessionEntity session) {
        return session != null && "PROCESSING".equals(session.getStatus());
    }

    default InvoiceConfirmResponseModel mapConfirmResponse(InvoiceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new InvoiceConfirmResponseModel(
                new InvoiceSummaryModel(
                        entity.getId(),
                        entity.getInvoiceNumber(),
                        entity.getInvoiceType(),
                        entity.getIssueDate() == null ? null : entity.getIssueDate().toString(),
                        entity.getTotalAmount(),
                        entity.getInvoiceStatus()
                ),
                entity.getConfirmedFields(),
                entity.getOcrFields()
        );
    }

    default InvoicePreviewResponseModel mapPreview(InvoiceFileAccessEntity entity) {
        if (entity == null) {
            return null;
        }
        return new InvoicePreviewResponseModel(
                entity.getInvoiceId(),
                entity.getFileId(),
                entity.getUrl(),
                entity.getMethod(),
                entity.getExpiresInSeconds(),
                entity.getExpiresAt(),
                entity.getFilename(),
                entity.getContentType()
        );
    }

    default InvoiceSummaryModel mapSummaryModel(InvoiceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new InvoiceSummaryModel(
                entity.getId(),
                entity.getInvoiceNumber(),
                entity.getInvoiceType(),
                entity.getIssueDate() == null ? null : entity.getIssueDate().toString(),
                entity.getTotalAmount(),
                entity.getInvoiceStatus()
        );
    }

    default InvoiceRecognizeUploadResponseModel mapUploadResponse(
            String batchId,
            List<InvoiceRecognizeSessionEntity> sessions,
            long nextPollAfterMs
    ) {
        InvoiceRecognizeSessionEntity first = sessions == null || sessions.isEmpty() ? null : sessions.getFirst();
        return new InvoiceRecognizeUploadResponseModel(
                batchId,
                first == null ? null : first.getId(),
                first == null ? null : first.getStatus(),
                first == null ? null : first.getExpiresAt(),
                nextPollAfterMs,
                sessions == null ? List.of() : sessions.stream().map(this::mapSummary).toList()
        );
    }

    default InvoiceRecognizeSessionSummaryModel mapSummary(InvoiceRecognizeSessionEntity entity) {
        if (entity == null) {
            return null;
        }
        String fileName = entity.getFiles() == null || entity.getFiles().isEmpty()
                ? null
                : entity.getFiles().getFirst().getFileName();
        return new InvoiceRecognizeSessionSummaryModel(
                entity.getId(),
                fileName,
                entity.getStatus(),
                entity.getExpiresAt()
        );
    }
}
