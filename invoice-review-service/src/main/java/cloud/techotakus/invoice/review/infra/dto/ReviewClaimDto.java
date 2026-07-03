package cloud.techotakus.invoice.review.infra.dto;

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
public class ReviewClaimDto {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("status")
    private String status;

    @TableField("latest_review_opinion")
    private String latestReviewOpinion;

    @TableField("claim_no")
    private String claimNo;

    @TableField("applicant_user_id")
    private String applicantUserId;

    @TableField("description")
    private String description;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("currency")
    private String currency;

    @TableField(exist = false)
    private Integer invoiceCount;

    @TableField("submitted_at")
    private OffsetDateTime submittedAt;

    @TableField("approved_at")
    private OffsetDateTime approvedAt;

    @TableField("created_at")
    private OffsetDateTime createdAt;

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
