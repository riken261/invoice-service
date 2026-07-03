package cloud.techotakus.invoice.core.infra.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import cloud.techotakus.invoice.core.infra.typehandler.InvoiceJsonbTypeHandler;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@TableName(value = "ocr_recognition_sessions", autoResultMap = true)
public class InvoiceRecognizeSessionDto {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("batch_id")
    private String batchId;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("invoice_file_id")
    private String invoiceFileId;

    @TableField("owner_user_id")
    private String ownerUserId;

    @TableField("idempotency_key")
    private String idempotencyKey;

    @TableField("source")
    private String source;

    @TableField("provider")
    private String provider;

    @TableField("status")
    private String status;

    @TableField("raw_request_id")
    private String rawRequestId;

    @TableField(value = "raw_result", typeHandler = InvoiceJsonbTypeHandler.class)
    private Map<String, Object> rawResult;

    @TableField(value = "normalized_result", typeHandler = InvoiceJsonbTypeHandler.class)
    private Map<String, Object> normalizedResult;

    @TableField("error_code")
    private String errorCode;

    @TableField("error_message")
    private String errorMessage;

    @TableField("started_at")
    private OffsetDateTime startedAt;

    @TableField("finished_at")
    private OffsetDateTime finishedAt;

    @TableField("expires_at")
    private OffsetDateTime expiresAt;

    @TableField("submitted_invoice_id")
    private String submittedInvoiceId;

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
