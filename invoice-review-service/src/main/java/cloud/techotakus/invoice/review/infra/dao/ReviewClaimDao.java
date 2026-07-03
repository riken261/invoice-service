package cloud.techotakus.invoice.review.infra.dao;

import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimDetailEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimInvoiceEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimPendingEntity;
import cloud.techotakus.invoice.review.domain.entity.ReviewClaimReviewRecordEntity;
import cloud.techotakus.invoice.review.domain.repository.ReviewClaimRepository;
import cloud.techotakus.invoice.review.infra.dto.ReviewClaimDto;
import cloud.techotakus.invoice.review.infra.mapper.ReviewClaimMapper;
import cloud.techotakus.invoice.review.infra.mapper.ReviewClaimInvoiceMapper;
import cloud.techotakus.invoice.review.infra.mapper.ReviewClaimReviewRecordMapper;
import cloud.techotakus.invoice.review.infra.dto.ReviewClaimReviewRecordDto;
import cloud.techotakus.invoice.review.infra.mapstruct.ReviewClaimInfraMapstruct;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReviewClaimDao implements ReviewClaimRepository {

    @Resource
    private ReviewClaimMapper mapper;

    @Resource
    private ReviewClaimInvoiceMapper invoiceMapper;

    @Resource
    private ReviewClaimReviewRecordMapper reviewRecordMapper;

    @Resource
    private ReviewClaimInfraMapstruct mapstruct;

    @Override
    public PageResponse<ReviewClaimPendingEntity> pending(String tenantId, int page, int pageSize) {
        IPage<ReviewClaimDto> result = mapper.selectPendingClaims(Page.of(page, pageSize), tenantId);
        List<ReviewClaimPendingEntity> items = result.getRecords().stream()
                .map(mapstruct::map)
                .toList();
        return PageResponse.success(items, page, pageSize, result.getTotal());
    }

    @Override
    public ReviewClaimDetailEntity findDetail(String tenantId, String claimId) {
        return mapstruct.mapDetail(mapper.selectOne(new LambdaQueryWrapper<ReviewClaimDto>()
                .eq(ReviewClaimDto::getId, claimId)
                .eq(ReviewClaimDto::getTenantId, tenantId)
                .eq(ReviewClaimDto::getDeleted, false)
                .last("LIMIT 1")));
    }

    @Override
    public List<ReviewClaimInvoiceEntity> findClaimInvoices(String tenantId, String claimId) {
        return invoiceMapper.selectByClaimId(tenantId, claimId)
                .stream()
                .map(mapstruct::map)
                .toList();
    }

    @Override
    public List<ReviewClaimReviewRecordEntity> findReviewRecords(String tenantId, String claimId) {
        return reviewRecordMapper.selectList(new LambdaQueryWrapper<ReviewClaimReviewRecordDto>()
                        .eq(ReviewClaimReviewRecordDto::getTenantId, tenantId)
                        .eq(ReviewClaimReviewRecordDto::getResourceType, "EXPENSE_CLAIM")
                        .eq(ReviewClaimReviewRecordDto::getResourceId, claimId)
                        .eq(ReviewClaimReviewRecordDto::getDeleted, false)
                        .orderByAsc(ReviewClaimReviewRecordDto::getCreatedAt))
                .stream()
                .map(mapstruct::map)
                .toList();
    }

    @Override
    public void updateClaim(ReviewClaimDetailEntity claim) {
        mapper.updateById(mapstruct.map(claim));
    }

    @Override
    public void saveReviewRecord(ReviewClaimReviewRecordEntity record) {
        reviewRecordMapper.insert(mapstruct.map(record));
    }
}
