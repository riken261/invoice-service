package cloud.techotakus.invoice.authorization.infra.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
@TableName("users")
public class AuthUserDto {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("keycloak_subject")
    private String keycloakSubject;

    @TableField("username")
    private String username;

    @TableField("display_name")
    private String displayName;

    @TableField("email")
    private String email;

    @TableField("department_id")
    private String departmentId;

    @TableField("source_type")
    private String sourceType;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("last_login_at")
    private OffsetDateTime lastLoginAt;

    @TableLogic(value = "false", delval = "true")
    @TableField("deleted")
    private Boolean deleted;
}
