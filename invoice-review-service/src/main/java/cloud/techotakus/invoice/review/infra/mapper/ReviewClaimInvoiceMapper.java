package cloud.techotakus.invoice.review.infra.mapper;

import cloud.techotakus.invoice.review.infra.dto.ReviewClaimInvoiceDto;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewClaimInvoiceMapper extends BaseMapper<ReviewClaimInvoiceDto> {

    @Select("""
            SELECT
                i.id,
                i.tenant_id,
                i.invoice_number,
                i.seller_name,
                item.amount AS total_amount,
                COALESCE(c.currency, i.currency) AS currency,
                i.invoice_status,
                i.created_at,
                item.expense_category,
                COALESCE(f.original_filename, i.invoice_file_id, i.id) AS file_name
            FROM expense_claim_items item
            JOIN expense_claims c
                ON c.id = item.claim_id
                AND c.tenant_id = item.tenant_id
                AND c.deleted = false
            JOIN invoices i
                ON i.id = item.invoice_id
                AND i.tenant_id = item.tenant_id
                AND i.deleted = false
            LEFT JOIN invoice_files f
                ON f.id = i.invoice_file_id
                AND f.deleted = false
            WHERE item.tenant_id = #{tenantId}
                AND item.claim_id = #{claimId}
                AND item.deleted = false
            ORDER BY item.created_at ASC
            """)
    List<ReviewClaimInvoiceDto> selectByClaimId(
            @Param("tenantId") String tenantId,
            @Param("claimId") String claimId
    );
}
