package cloud.techotakus.invoice.core.app.mapstruct;

import cloud.techotakus.invoice.core.api.model.InvoiceDetailResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoiceDownloadItemModel;
import cloud.techotakus.invoice.core.api.model.InvoiceDownloadResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoicePreviewResponseModel;
import cloud.techotakus.invoice.core.api.model.InvoiceSummaryModel;
import cloud.techotakus.invoice.core.domain.entity.InvoiceEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceFileAccessEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvoiceRecordMapstruct {

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

    default InvoiceDetailResponseModel mapDetail(InvoiceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new InvoiceDetailResponseModel(
                entity.getId(),
                entity.getInvoiceFileId(),
                entity.getInvoiceNumber(),
                entity.getInvoiceType(),
                entity.getInvoiceCode(),
                entity.getIssueDate() == null ? null : entity.getIssueDate().toString(),
                entity.getSellerName(),
                entity.getBuyerName(),
                entity.getAmountWithoutTax(),
                entity.getTaxAmount(),
                entity.getTotalAmount(),
                entity.getCurrency(),
                entity.getOcrStatus(),
                entity.getInvoiceStatus(),
                entity.getDuplicateStatus(),
                entity.getManualInput(),
                entity.getOcrFields(),
                entity.getConfirmedFields(),
                entity.getManualFields(),
                entity.getLatestReviewOpinion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
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

    default InvoiceDownloadItemModel mapDownloadItem(InvoiceFileAccessEntity entity) {
        if (entity == null) {
            return null;
        }
        return new InvoiceDownloadItemModel(
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

    default InvoiceDownloadResponseModel mapDownload(List<InvoiceFileAccessEntity> entities) {
        return new InvoiceDownloadResponseModel(
                entities == null ? List.of() : entities.stream().map(this::mapDownloadItem).toList()
        );
    }
}
