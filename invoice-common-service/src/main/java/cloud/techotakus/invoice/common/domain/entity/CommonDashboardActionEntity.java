package cloud.techotakus.invoice.common.domain.entity;

public record CommonDashboardActionEntity(
    String actionCode,
    String actionName,
    String groupCode,
    String groupName,
    String routerPath,
    String permissionCode,
    String i18nKey,
    String badgeCode,
    Integer sortOrder
) {
}
