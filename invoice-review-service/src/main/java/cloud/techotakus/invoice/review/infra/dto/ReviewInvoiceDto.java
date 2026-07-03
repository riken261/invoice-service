package cloud.techotakus.invoice.review.infra.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("invoices")
public class ReviewInvoiceDto {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("invoice_status")
    private String invoiceStatus;

    @TableField("duplicate_status")
    private String duplicateStatus;

    @TableLogic(value = "false", delval = "true")
    @TableField("deleted")
    private Boolean deleted;
}
