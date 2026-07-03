package cloud.techotakus.invoice.core.infra.dto;

import cloud.techotakus.invoice.core.infra.typehandler.InvoiceJsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@TableName(value = "invoices", autoResultMap = true)
public class InvoiceDto {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("invoice_file_id")
    private String invoiceFileId;

    @TableField("owner_user_id")
    private String ownerUserId;

    @TableField("invoice_type")
    private String invoiceType;

    @TableField("invoice_code")
    private String invoiceCode;

    @TableField("invoice_number")
    private String invoiceNumber;

    @TableField("issue_date")
    private LocalDate issueDate;

    @TableField("seller_name")
    private String sellerName;

    @TableField("buyer_name")
    private String buyerName;

    @TableField("amount_without_tax")
    private BigDecimal amountWithoutTax;

    @TableField("tax_amount")
    private BigDecimal taxAmount;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("currency")
    private String currency;

    @TableField("ocr_status")
    private String ocrStatus;

    @TableField("invoice_status")
    private String invoiceStatus;

    @TableField("duplicate_status")
    private String duplicateStatus;

    @TableField("manual_input")
    private Boolean manualInput;

    @TableField(value = "ocr_fields", typeHandler = InvoiceJsonbTypeHandler.class)
    private Map<String, Object> ocrFields;

    @TableField(value = "confirmed_fields", typeHandler = InvoiceJsonbTypeHandler.class)
    private Map<String, Object> confirmedFields;

    @TableField(value = "manual_fields", typeHandler = InvoiceJsonbTypeHandler.class)
    private Map<String, Object> manualFields;

    @TableField("latest_review_opinion")
    private String latestReviewOpinion;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(value = "created_trace", fill = FieldFill.INSERT)
    private String createdTrace;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableField(value = "updated_trace", fill = FieldFill.INSERT_UPDATE)
    private String updatedTrace;

    @TableLogic(value = "false", delval = "true")
    @TableField("deleted")
    private Boolean deleted;
}
