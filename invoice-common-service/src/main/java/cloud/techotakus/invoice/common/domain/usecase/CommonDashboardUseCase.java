package cloud.techotakus.invoice.common.domain.usecase;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardActionEntity;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardActionGroupEntity;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardActionItemEntity;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardActionMenusEntity;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardEmployeeSummaryEntity;
import cloud.techotakus.invoice.common.domain.entity.CommonDashboardFinanceSummaryEntity;
import cloud.techotakus.invoice.common.domain.repository.CommonDashboardSummaryRepository;
import cloud.techotakus.invoice.common.domain.repository.CommonDashboardRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CommonDashboardUseCase {

    private final CommonDashboardRepository repository;
    private final CommonDashboardSummaryRepository summaryRepository;

    public CommonDashboardUseCase(
            CommonDashboardRepository repository,
            CommonDashboardSummaryRepository summaryRepository
    ) {
        this.repository = repository;
        this.summaryRepository = summaryRepository;
    }

    public CommonDashboardActionMenusEntity actionMenus(String permissionsHeader) {
        Set<String> permissions = permissions(permissionsHeader);
        if (permissions.isEmpty()) {
            throw new ServiceException("permission denied", ErrorCode.PERMISSION_DENIED);
        }

        var groups = new LinkedHashMap<String, DashboardActionGroupBuilder>();
        for (CommonDashboardActionEntity action : repository.enabledActions()) {
            if (!hasPermission(permissions, action.permissionCode())) {
                continue;
            }
            groups.computeIfAbsent(
                    action.groupCode(),
                    groupCode -> new DashboardActionGroupBuilder(groupCode, action.groupName())
                )
                .actions()
                .add(new CommonDashboardActionItemEntity(
                    action.i18nKey(),
                    action.actionName(),
                    action.routerPath(),
                    action.permissionCode(),
                    0,
                    true
                ));
        }

        return new CommonDashboardActionMenusEntity(groups.values().stream()
            .filter(group -> !group.actions().isEmpty())
            .map(group -> new CommonDashboardActionGroupEntity(group.code(), group.name(), List.copyOf(group.actions())))
            .toList());
    }

    public CommonDashboardEmployeeSummaryEntity employeeSummary(String tenantId, String userId, String permissionsHeader) {
        Set<String> permissions = permissions(permissionsHeader);
        if (!permissions.contains("invoice:view:self")) {
            throw new ServiceException("permission denied", ErrorCode.PERMISSION_DENIED);
        }

        var invoice = summaryRepository.invoiceSummary(tenantId, userId);
        var claim = summaryRepository.claimSummary(tenantId, userId);
        return new CommonDashboardEmployeeSummaryEntity(
            invoice.ocrProcessingCount(),
            invoice.ocrConfirmRequiredCount(),
            invoice.manualInputRequiredCount(),
            invoice.rejectedInvoiceCount(),
            claim.draftClaimCount(),
            claim.rejectedClaimCount()
        );
    }

    public CommonDashboardFinanceSummaryEntity financeSummary(String tenantId, String reviewerId, String permissionsHeader) {
        Set<String> permissions = permissions(permissionsHeader);
        if (!permissions.contains("claim:finance-review")) {
            throw new ServiceException("permission denied", ErrorCode.PERMISSION_DENIED);
        }

        return summaryRepository.financeSummary(tenantId, reviewerId);
    }

    private boolean hasPermission(Set<String> permissions, String permissionCode) {
        return !StringUtils.hasText(permissionCode) || permissions.contains(permissionCode);
    }

    private Set<String> permissions(String permissionsHeader) {
        if (!StringUtils.hasText(permissionsHeader)) {
            return Set.of();
        }
        return Arrays.stream(permissionsHeader.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .collect(Collectors.toUnmodifiableSet());
    }

    private record DashboardActionGroupBuilder(
        String code,
        String name,
        List<CommonDashboardActionItemEntity> actions
    ) {
        private DashboardActionGroupBuilder(String code, String name) {
            this(code, name, new ArrayList<>());
        }
    }
}
