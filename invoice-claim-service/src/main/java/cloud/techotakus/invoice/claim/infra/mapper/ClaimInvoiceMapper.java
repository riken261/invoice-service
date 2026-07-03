package cloud.techotakus.invoice.claim.infra.mapper;

import cloud.techotakus.invoice.claim.infra.dto.ClaimInvoiceDto;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClaimInvoiceMapper extends BaseMapper<ClaimInvoiceDto> {

    @Select("""
            SELECT
                i.id,
                i.tenant_id,
                i.invoice_file_id,
                i.owner_user_id,
                i.invoice_number,
                i.seller_name,
                i.total_amount,
                i.currency,
                i.invoice_status,
                i.created_at,
                COALESCE(f.original_filename, i.invoice_file_id, i.id) AS file_name,
                'office' AS expense_category
            FROM invoices i
            LEFT JOIN invoice_files f
                ON f.id = i.invoice_file_id
                AND f.deleted = false
            WHERE i.tenant_id = #{tenantId}
                AND i.owner_user_id = #{userId}
                AND i.deleted = false
                AND i.invoice_status IN ('USER_CONFIRMED', 'CLAIMABLE', 'CONFIRMED', 'OCR_CONFIRMED')
                AND NOT EXISTS (
                    SELECT 1
                    FROM expense_claim_items item
                    WHERE item.tenant_id = i.tenant_id
                        AND item.invoice_id = i.id
                        AND item.deleted = false
                )
            ORDER BY i.created_at DESC
            """)
    IPage<ClaimInvoiceDto> selectClaimablePage(
            Page<ClaimInvoiceDto> page,
            @Param("tenantId") String tenantId,
            @Param("userId") String userId
    );

    @Select("""
            <script>
            SELECT
                i.id,
                i.tenant_id,
                i.invoice_file_id,
                i.owner_user_id,
                i.invoice_number,
                i.seller_name,
                i.total_amount,
                i.currency,
                i.invoice_status,
                i.created_at,
                COALESCE(f.original_filename, i.invoice_file_id, i.id) AS file_name,
                'office' AS expense_category
            FROM invoices i
            LEFT JOIN invoice_files f
                ON f.id = i.invoice_file_id
                AND f.deleted = false
            WHERE i.tenant_id = #{tenantId}
                AND i.owner_user_id = #{userId}
                AND i.deleted = false
                AND i.id IN
                <foreach collection="invoiceIds" item="invoiceId" open="(" separator="," close=")">
                    #{invoiceId}
                </foreach>
                AND i.invoice_status IN ('USER_CONFIRMED', 'CLAIMABLE', 'CONFIRMED', 'OCR_CONFIRMED')
                AND NOT EXISTS (
                    SELECT 1
                    FROM expense_claim_items item
                    WHERE item.tenant_id = i.tenant_id
                        AND item.invoice_id = i.id
                        AND item.deleted = false
                )
            </script>
            """)
    List<ClaimInvoiceDto> selectClaimableByIds(
            @Param("tenantId") String tenantId,
            @Param("userId") String userId,
            @Param("invoiceIds") List<String> invoiceIds
    );

    @Select("""
            SELECT
                i.id,
                i.tenant_id,
                i.invoice_file_id,
                i.owner_user_id,
                i.invoice_number,
                i.seller_name,
                item.amount AS total_amount,
                COALESCE(ec.currency, i.currency) AS currency,
                i.invoice_status,
                i.created_at,
                COALESCE(f.original_filename, i.invoice_file_id, i.id) AS file_name,
                item.expense_category
            FROM expense_claim_items item
            JOIN expense_claims ec
                ON ec.id = item.claim_id
                AND ec.tenant_id = item.tenant_id
                AND ec.deleted = false
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
    List<ClaimInvoiceDto> selectClaimInvoices(
            @Param("tenantId") String tenantId,
            @Param("claimId") String claimId
    );
}
