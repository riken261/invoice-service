package cloud.techotakus.invoice.review.infra.mapper;

import cloud.techotakus.invoice.review.infra.dto.ReviewClaimDto;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface ReviewClaimMapper extends BaseMapper<ReviewClaimDto> {

    @Select("""
            SELECT AVG(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - COALESCE(submitted_at, created_at))) / 3600.0)
            FROM expense_claims
            WHERE tenant_id = #{tenantId} AND status IN ('SUBMITTED', 'FINANCE_REVIEWING') AND deleted = false
            """)
    BigDecimal averagePendingClaimWaitingHours(String tenantId);

    @Select("""
            SELECT
                c.id,
                c.tenant_id,
                c.claim_no,
                c.applicant_user_id,
                c.description,
                c.total_amount,
                c.currency,
                c.status,
                c.submitted_at,
                c.created_at,
                COUNT(item.id) AS invoice_count
            FROM expense_claims c
            LEFT JOIN expense_claim_items item
                ON item.tenant_id = c.tenant_id
                AND item.claim_id = c.id
                AND item.deleted = false
            WHERE c.tenant_id = #{tenantId}
                AND c.status IN ('SUBMITTED', 'FINANCE_REVIEWING')
                AND c.deleted = false
            GROUP BY
                c.id,
                c.tenant_id,
                c.claim_no,
                c.applicant_user_id,
                c.description,
                c.total_amount,
                c.currency,
                c.status,
                c.submitted_at,
                c.created_at
            ORDER BY COALESCE(c.submitted_at, c.created_at) ASC
            """)
    IPage<ReviewClaimDto> selectPendingClaims(Page<ReviewClaimDto> page, @Param("tenantId") String tenantId);
}
