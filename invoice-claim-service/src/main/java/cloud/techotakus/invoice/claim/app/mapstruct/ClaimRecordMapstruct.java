package cloud.techotakus.invoice.claim.app.mapstruct;

import cloud.techotakus.invoice.claim.api.model.ClaimRecordResponseModel;
import cloud.techotakus.invoice.claim.api.model.ClaimDetailResponseModel;
import cloud.techotakus.invoice.claim.api.model.ClaimReviewRecordResponseModel;
import cloud.techotakus.invoice.claim.api.model.ClaimSaveRequestModel;
import cloud.techotakus.invoice.claim.api.model.ClaimSubmitRequestModel;
import cloud.techotakus.invoice.claim.api.model.ClaimSubmitResponseModel;
import cloud.techotakus.invoice.claim.domain.entity.ClaimDetailEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimRecordEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimReviewRecordEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimSubmitEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClaimRecordMapstruct {

    default ClaimRecordResponseModel map(ClaimRecordEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ClaimRecordResponseModel(
                entity.getId(),
                entity.getId(),
                entity.getInvoiceNumber(),
                entity.getSellerName(),
                entity.getSellerName(),
                entity.getTotalAmount(),
                entity.getTotalAmount(),
                entity.getCurrency(),
                entity.getExpenseCategory(),
                entity.getFileName(),
                entity.getInvoiceStatus(),
                entity.getCreatedAt()
        );
    }

    default ClaimSubmitEntity map(ClaimSaveRequestModel request) {
        if (request == null) {
            return null;
        }
        ClaimSubmitEntity entity = new ClaimSubmitEntity();
        entity.setNonce(request.nonce());
        entity.setIdempotencyKey(request.idempotencyKey());
        entity.setTitle(request.title());
        entity.setDescription(request.description() == null ? request.costCenter() : request.description());
        entity.setExpenseCategory(request.expenseCategory());
        entity.setInvoiceIds(request.invoiceIds());
        return entity;
    }

    default ClaimSubmitEntity map(ClaimSubmitRequestModel request) {
        if (request == null) {
            return null;
        }
        ClaimSubmitEntity entity = new ClaimSubmitEntity();
        entity.setNonce(request.nonce());
        entity.setIdempotencyKey(request.idempotencyKey());
        return entity;
    }

    default ClaimSubmitResponseModel mapSubmit(ClaimSubmitEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ClaimSubmitResponseModel(
                entity.getId(),
                entity.getId(),
                entity.getClaimNo(),
                entity.getStatus(),
                entity.getTotalAmount(),
                entity.getCurrency(),
                entity.getInvoiceIds(),
                entity.getSubmittedAt()
        );
    }

    default ClaimDetailResponseModel mapDetail(ClaimDetailEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ClaimDetailResponseModel(
                entity.getId(),
                entity.getId(),
                entity.getClaimNo(),
                entity.getApplicantUserId(),
                entity.getDescription(),
                entity.getDescription(),
                entity.getDescription(),
                entity.getTotalAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getExpenseCategory(),
                entity.getInvoiceIds(),
                entity.getInvoices() == null ? List.of() : entity.getInvoices().stream().map(this::map).toList(),
                entity.getSubmittedAt(),
                entity.getCreatedAt()
        );
    }

    default ClaimReviewRecordResponseModel mapReviewRecord(ClaimReviewRecordEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ClaimReviewRecordResponseModel(
                entity.getId(),
                entity.getAction(),
                entity.getReviewerUserId(),
                entity.getReviewerUserId(),
                entity.getOpinion(),
                entity.getBeforeStatus(),
                entity.getAfterStatus(),
                entity.getCreatedAt(),
                entity.getCreatedAt()
        );
    }
}
