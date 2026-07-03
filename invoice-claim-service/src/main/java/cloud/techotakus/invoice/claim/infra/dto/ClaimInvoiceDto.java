package cloud.techotakus.invoice.claim.infra.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@TableName("invoices")
public class ClaimInvoiceDto {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("invoice_file_id")
    private String invoiceFileId;

    @TableField("owner_user_id")
    private String ownerUserId;

    @TableField("invoice_number")
    private String invoiceNumber;

    @TableField("seller_name")
    private String sellerName;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("currency")
    private String currency;

    @TableField("invoice_status")
    private String invoiceStatus;

    @TableField("created_at")
    private OffsetDateTime createdAt;

    @TableField(exist = false)
    private String expenseCategory;

    @TableField(exist = false)
    private String fileName;

    @TableLogic(value = "false", delval = "true")
    @TableField("deleted")
    private Boolean deleted;
}
