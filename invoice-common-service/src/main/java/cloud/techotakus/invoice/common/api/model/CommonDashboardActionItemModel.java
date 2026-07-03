package cloud.techotakus.invoice.common.api.model;

public record CommonDashboardActionItemModel(
    String i18nKey,
    String actionName,
    String routerPath,
    String permissionCode,
    long badgeCount,
    boolean enabled
) {
}
