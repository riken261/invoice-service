package cloud.techotakus.invoice.claim.infra.dao;

import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.claim.domain.entity.ClaimDetailEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimItemEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimRecordEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimReviewRecordEntity;
import cloud.techotakus.invoice.claim.domain.entity.ClaimSubmitEntity;
import cloud.techotakus.invoice.claim.domain.repository.ClaimRecordRepository;
import cloud.techotakus.invoice.claim.infra.dto.ClaimInvoiceDto;
import cloud.techotakus.invoice.claim.infra.dto.ClaimItemDto;
import cloud.techotakus.invoice.claim.infra.mapper.ClaimItemMapper;
import cloud.techotakus.invoice.claim.infra.mapper.ClaimMapper;
import cloud.techotakus.invoice.claim.infra.mapper.ClaimInvoiceMapper;
import cloud.techotakus.invoice.claim.infra.mapper.ClaimReviewRecordMapper;
import cloud.techotakus.invoice.claim.infra.dto.ClaimReviewRecordDto;
import cloud.techotakus.invoice.claim.infra.mapstruct.ClaimRecordInfraMapstruct;
import cloud.techotakus.invoice.claim.infra.dto.ClaimDto;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class ClaimRecordDao implements ClaimRecordRepository {

    @Resource
    private ClaimInvoiceMapper mapper;

    @Resource
    private ClaimMapper claimMapper;

    @Resource
    private ClaimItemMapper itemMapper;

    @Resource
    private ClaimReviewRecordMapper reviewRecordMapper;

    @Resource
    private ClaimRecordInfraMapstruct mapstruct;

    @Override
    public PageResponse<ClaimRecordEntity> listClaimable(String tenantId, String userId, int page, int pageSize) {
        IPage<ClaimInvoiceDto> result = mapper.selectClaimablePage(Page.of(page, pageSize), tenantId, userId);
        List<ClaimRecordEntity> items = result.getRecords().stream()
                .map(mapstruct::map)
                .toList();
        return PageResponse.success(items, page, pageSize, result.getTotal());
    }

    @Override
    public PageResponse<ClaimDetailEntity> listClaims(String tenantId, String userId, int page, int pageSize) {
        IPage<ClaimDto> result = claimMapper.selectPage(
                Page.of(page, pageSize),
                new LambdaQueryWrapper<ClaimDto>()
                        .eq(ClaimDto::getTenantId, tenantId)
                        .eq(ClaimDto::getApplicantUserId, userId)
                        .eq(ClaimDto::getDeleted, false)
                        .orderByDesc(ClaimDto::getCreatedAt)
        );
        List<ClaimDetailEntity> items = result.getRecords().stream()
                .map(mapstruct::mapDetail)
                .toList();
        return PageResponse.success(items, page, pageSize, result.getTotal());
    }

    @Override
    public ClaimDetailEntity findDetail(String tenantId, String userId, String claimId) {
        return mapstruct.mapDetail(findOwnedClaim(tenantId, userId, claimId));
    }

    @Override
    public List<ClaimRecordEntity> findClaimableInvoices(String tenantId, String userId, List<String> invoiceIds) {
        return mapper.selectClaimableByIds(tenantId, userId, invoiceIds)
                .stream()
                .map(mapstruct::map)
                .toList();
    }

    @Override
    public List<ClaimRecordEntity> findClaimInvoices(String tenantId, String claimId) {
        return mapper.selectClaimInvoices(tenantId, claimId)
                .stream()
                .map(mapstruct::map)
                .toList();
    }

    @Override
    public List<ClaimReviewRecordEntity> findReviewRecords(String tenantId, String claimId) {
        return reviewRecordMapper.selectList(new LambdaQueryWrapper<ClaimReviewRecordDto>()
                        .eq(ClaimReviewRecordDto::getTenantId, tenantId)
                        .eq(ClaimReviewRecordDto::getResourceType, "EXPENSE_CLAIM")
                        .eq(ClaimReviewRecordDto::getResourceId, claimId)
                        .eq(ClaimReviewRecordDto::getDeleted, false)
                        .orderByAsc(ClaimReviewRecordDto::getCreatedAt))
                .stream()
                .map(mapstruct::map)
                .toList();
    }

    @Override
    public ClaimSubmitEntity findClaim(String tenantId, String userId, String claimId) {
        return mapstruct.map(findOwnedClaim(tenantId, userId, claimId));
    }

    private ClaimDto findOwnedClaim(String tenantId, String userId, String claimId) {
        return claimMapper.selectOne(new LambdaQueryWrapper<ClaimDto>()
                .eq(ClaimDto::getId, claimId)
                .eq(ClaimDto::getTenantId, tenantId)
                .eq(ClaimDto::getApplicantUserId, userId)
                .eq(ClaimDto::getDeleted, false)
                .last("LIMIT 1"));
    }

    @Override
    public void saveClaim(ClaimSubmitEntity claim) {
        claimMapper.insert(mapstruct.map(claim));
    }

    @Override
    public void updateClaim(ClaimSubmitEntity claim) {
        claimMapper.updateById(mapstruct.map(claim));
    }

    @Override
    public void updateDraftClaim(ClaimSubmitEntity claim) {
        claimMapper.update(null, new LambdaUpdateWrapper<ClaimDto>()
                .set(ClaimDto::getDescription, claim.getDescription())
                .set(ClaimDto::getStatus, claim.getStatus())
                .set(ClaimDto::getTotalAmount, claim.getTotalAmount())
                .set(ClaimDto::getCurrency, claim.getCurrency())
                .set(ClaimDto::getSubmittedAt, null)
                .set(ClaimDto::getLatestReviewOpinion, null)
                .set(ClaimDto::getUpdatedAt, claim.getUpdatedAt())
                .set(ClaimDto::getUpdatedBy, claim.getUpdatedBy())
                .set(ClaimDto::getUpdatedTrace, claim.getUpdatedTrace())
                .eq(ClaimDto::getId, claim.getId())
                .eq(ClaimDto::getTenantId, claim.getTenantId())
                .eq(ClaimDto::getApplicantUserId, claim.getApplicantUserId())
                .eq(ClaimDto::getDeleted, false));
    }

    @Override
    public void saveClaimItem(ClaimItemEntity item) {
        itemMapper.insert(mapstruct.map(item));
    }

    @Override
    public void deleteClaimItems(String tenantId, String claimId, OffsetDateTime updatedAt, String updatedBy, String updatedTrace) {
        itemMapper.update(null, new LambdaUpdateWrapper<ClaimItemDto>()
                .set(ClaimItemDto::getDeleted, true)
                .set(ClaimItemDto::getUpdatedAt, updatedAt)
                .set(ClaimItemDto::getUpdatedBy, updatedBy)
                .set(ClaimItemDto::getUpdatedTrace, updatedTrace)
                .eq(ClaimItemDto::getTenantId, tenantId)
                .eq(ClaimItemDto::getClaimId, claimId)
                .eq(ClaimItemDto::getDeleted, false));
    }
}
