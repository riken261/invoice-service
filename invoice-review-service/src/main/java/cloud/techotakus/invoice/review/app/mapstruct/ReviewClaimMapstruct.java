package cloud.techotakus.invoice.review.app.mapstruct;

import cloud.techotakus.invoice.review.api.model.ReviewClaimDetailResponseModel;
import cloud.techotakus.invoice.review.api.model.ReviewClaimDecisionRequestModel;
import cloud.techotakus.invoice.review.api.model.ReviewClaimDecisionResponseModel;
import cloud.techotakus.invoice.review.api.model.ReviewClaimInvoiceResponseModel;
import cloud.techotakus.invoice.review.api.model.ReviewClaimPendingResponseModel;
import cloud.techotakus.invoice.review.api.model.ReviewClaimReviewRecordResponseModel;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimDecisionEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimDetailEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimInvoiceEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimPendingEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimReviewRecordEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewClaimMapstruct {

    default ReviewClaimPendingResponseModel mapPending(ReviewClaimPendingEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ReviewClaimPendingResponseModel(
                entity.getId(),
                entity.getId(),
                entity.getId(),
                entity.getClaimNo(),
                entity.getDescription(),
                entity.getApplicantUserId(),
                entity.getApplicantUserId(),
                entity.getTotalAmount(),
                entity.getTotalAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getInvoiceCount(),
                entity.getSubmittedAt(),
                entity.getCreatedAt()
        );
    }

    default ReviewClaimDetailResponseModel mapDetail(ReviewClaimDetailEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ReviewClaimDetailResponseModel(
                entity.getId(),
                entity.getId(),
                entity.getClaimNo(),
                entity.getApplicantUserId(),
                entity.getApplicantUserId(),
                entity.getDescription(),
                entity.getDescription(),
                entity.getDescription(),
                entity.getTotalAmount(),
                entity.getTotalAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getExpenseCategory(),
                entity.getInvoiceIds(),
                entity.getInvoices() == null ? List.of() : entity.getInvoices().stream().map(this::mapInvoice).toList(),
                entity.getSubmittedAt(),
                entity.getCreatedAt()
        );
    }

    default ReviewClaimDecisionEntity map(ReviewClaimDecisionRequestModel request) {
        if (request == null) {
            return null;
        }
        ReviewClaimDecisionEntity entity = new ReviewClaimDecisionEntity();
        entity.setNonce(request.nonce());
        entity.setIdempotencyKey(request.idempotencyKey());
        entity.setComment(request.comment());
        return entity;
    }

    default ReviewClaimDecisionResponseModel mapDecision(ReviewClaimDecisionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ReviewClaimDecisionResponseModel(
                entity.getClaimId(),
                entity.getStatus(),
                entity.getReviewId(),
                entity.getAction(),
                entity.getComment(),
                entity.getReviewedAt()
        );
    }

    default ReviewClaimInvoiceResponseModel mapInvoice(ReviewClaimInvoiceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ReviewClaimInvoiceResponseModel(
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

    default ReviewClaimReviewRecordResponseModel mapReviewRecord(ReviewClaimReviewRecordEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ReviewClaimReviewRecordResponseModel(
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
