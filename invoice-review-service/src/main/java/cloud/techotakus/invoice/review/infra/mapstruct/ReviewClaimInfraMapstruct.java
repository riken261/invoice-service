package cloud.techotakus.invoice.review.infra.mapstruct;

import cloud.techotakus.invoice.review.domain.entity.ReviewClaimDetailEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimInvoiceEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimPendingEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimReviewRecordEntity;
import cloud.techotakus.invoice.review.infra.dto.ReviewClaimInvoiceDto;
import cloud.techotakus.invoice.review.infra.dto.ReviewClaimReviewRecordDto;
import cloud.techotakus.invoice.review.infra.dto.ReviewClaimDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewClaimInfraMapstruct {

    ReviewClaimPendingEntity map(ReviewClaimDto dto);

    @Mapping(target = "expenseCategory", ignore = true)
    @Mapping(target = "invoiceIds", ignore = true)
    @Mapping(target = "invoices", ignore = true)
    ReviewClaimDetailEntity mapDetail(ReviewClaimDto dto);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "invoiceCount", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    ReviewClaimDto map(ReviewClaimDetailEntity entity);

    ReviewClaimInvoiceEntity map(ReviewClaimInvoiceDto dto);

    ReviewClaimReviewRecordEntity map(ReviewClaimReviewRecordDto dto);

    ReviewClaimReviewRecordDto map(ReviewClaimReviewRecordEntity entity);
}
