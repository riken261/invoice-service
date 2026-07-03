package cloud.techotakus.invoice.common.infra.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dashboard_actions")
public class CommonDashboardActionDto {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("action_code")
    private String actionCode;

    @TableField("action_name")
    private String actionName;

    @TableField("group_code")
    private String groupCode;

    @TableField("group_name")
    private String groupName;

    @TableField("router_path")
    private String routerPath;

    @TableField("permission_code")
    private String permissionCode;

    @TableField("i18n_key")
    private String i18nKey;

    @TableField("badge_code")
    private String badgeCode;

    @TableField(exist = false)
    private Integer sortOrder;

    @TableField("enabled")
    private Boolean enabled;

    @TableLogic(value = "false", delval = "true")
    @TableField("deleted")
    private Boolean deleted;
}
