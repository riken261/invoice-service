package cloud.techotakus.invoice.claim.infra.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@TableName("expense_claims")
public class ClaimDto {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("applicant_user_id")
    private String applicantUserId;

    @TableField("claim_no")
    private String claimNo;

    @TableField("department_id")
    private String departmentId;

    @TableField("description")
    private String description;

    @TableField("status")
    private String status;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("submitted_at")
    private OffsetDateTime submittedAt;

    @TableField("currency")
    private String currency;

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
