package cloud.techotakus.invoice.common.domain.entity;

public record CommonDashboardActionItemEntity(
    String i18nKey,
    String actionName,
    String routerPath,
    String permissionCode,
    long badgeCount,
    boolean enabled
) {
}
