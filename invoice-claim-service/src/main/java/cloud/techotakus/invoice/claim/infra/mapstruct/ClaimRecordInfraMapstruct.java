package cloud.techotakus.invoice.claim.infra.mapstruct;

import cloud.techotakus.invoice.claim.domain.entity.ClaimRecordEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimItemEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimDetailEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimSubmitEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimReviewRecordEntity;
import cloud.techotakus.invoice.claim.infra.dto.ClaimDto;
import cloud.techotakus.invoice.claim.infra.dto.ClaimInvoiceDto;
import cloud.techotakus.invoice.claim.infra.dto.ClaimItemDto;
import cloud.techotakus.invoice.claim.infra.dto.ClaimReviewRecordDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClaimRecordInfraMapstruct {

    ClaimRecordEntity map(ClaimInvoiceDto dto);

    @Mapping(target = "departmentId", ignore = true)
    @Mapping(target = "latestReviewOpinion", ignore = true)
    ClaimDto map(ClaimSubmitEntity entity);

    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "nonce", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "expenseCategory", ignore = true)
    @Mapping(target = "invoiceIds", ignore = true)
    ClaimSubmitEntity map(ClaimDto dto);

    @Mapping(target = "expenseCategory", ignore = true)
    @Mapping(target = "invoiceIds", ignore = true)
    @Mapping(target = "invoices", ignore = true)
    ClaimDetailEntity mapDetail(ClaimDto dto);

    ClaimReviewRecordEntity map(ClaimReviewRecordDto dto);

    ClaimItemDto map(ClaimItemEntity entity);
}
