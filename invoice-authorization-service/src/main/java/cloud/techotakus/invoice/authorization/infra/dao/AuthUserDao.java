package cloud.techotakus.invoice.authorization.infra.dao;

import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationPermissionSetEntity;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationUserEntity;
import cloud.techotakus.invoice.authorization.domain.repository.AuthUserRepository;
import cloud.techotakus.invoice.authorization.infra.dto.AuthPermissionDto;
import cloud.techotakus.invoice.authorization.infra.dto.AuthRoleDto;
import cloud.techotakus.invoice.authorization.infra.dto.AuthRolePermissionDto;
import cloud.techotakus.invoice.authorization.infra.dto.AuthTenantDto;
import cloud.techotakus.invoice.authorization.infra.dto.AuthUserDto;
import cloud.techotakus.invoice.authorization.infra.dto.AuthUserRoleDto;
import cloud.techotakus.invoice.authorization.infra.mapper.AuthPermissionMapper;
import cloud.techotakus.invoice.authorization.infra.mapper.AuthRoleMapper;
import cloud.techotakus.invoice.authorization.infra.mapper.AuthRolePermissionMapper;
import cloud.techotakus.invoice.authorization.infra.mapper.AuthTenantMapper;
import cloud.techotakus.invoice.authorization.infra.mapper.AuthUserMapper;
import cloud.techotakus.invoice.authorization.infra.mapper.AuthUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class AuthUserDao implements AuthUserRepository {

    private final AuthUserMapper userMapper;
    private final AuthTenantMapper tenantMapper;
    private final AuthUserRoleMapper userRoleMapper;
    private final AuthRoleMapper roleMapper;
    private final AuthRolePermissionMapper rolePermissionMapper;
    private final AuthPermissionMapper permissionMapper;

    public AuthUserDao(
        AuthUserMapper userMapper,
        AuthTenantMapper tenantMapper,
        AuthUserRoleMapper userRoleMapper,
        AuthRoleMapper roleMapper,
        AuthRolePermissionMapper rolePermissionMapper,
        AuthPermissionMapper permissionMapper
    ) {
        this.userMapper = userMapper;
        this.tenantMapper = tenantMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public Optional<String> findActiveTenantIdByCode(String tenantCode) {
        if (!StringUtils.hasText(tenantCode)) {
            return Optional.empty();
        }
        return Optional.ofNullable(tenantMapper.selectOne(new LambdaQueryWrapper<AuthTenantDto>()
                .eq(AuthTenantDto::getTenantCode, tenantCode)
                .eq(AuthTenantDto::getStatus, "ACTIVE")
                .last("LIMIT 1")))
            .map(AuthTenantDto::getId)
            .filter(StringUtils::hasText);
    }

    @Override
    public Optional<AuthorizationUserEntity> findEnabledUser(String tenantId, String keycloakSubject, String email) {
        if (StringUtils.hasText(keycloakSubject)) {
            Optional<AuthUserDto> bySubject = selectUser(new LambdaQueryWrapper<AuthUserDto>()
                .eq(AuthUserDto::getTenantId, tenantId)
                .eq(AuthUserDto::getKeycloakSubject, keycloakSubject)
                .eq(AuthUserDto::getEnabled, true)
                .last("LIMIT 1"));
            if (bySubject.isPresent()) {
                return bySubject.map(this::mapUser);
            }
        }
        if (!StringUtils.hasText(email)) {
            return Optional.empty();
        }
        return selectUser(new LambdaQueryWrapper<AuthUserDto>()
                .eq(AuthUserDto::getTenantId, tenantId)
                .eq(AuthUserDto::getEmail, email)
                .eq(AuthUserDto::getEnabled, true)
                .last("LIMIT 1"))
            .map(this::mapUser);
    }

    @Override
    public AuthorizationPermissionSetEntity permissions(String tenantId, String userId) {
        List<String> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<AuthUserRoleDto>()
                .eq(AuthUserRoleDto::getTenantId, tenantId)
                .eq(AuthUserRoleDto::getUserId, userId))
            .stream()
            .map(AuthUserRoleDto::getRoleId)
            .filter(StringUtils::hasText)
            .distinct()
            .toList();
        if (roleIds.isEmpty()) {
            return new AuthorizationPermissionSetEntity(List.of(), List.of());
        }

        List<AuthRoleDto> roles = roleMapper.selectList(new LambdaQueryWrapper<AuthRoleDto>()
            .and(wrapper -> wrapper.eq(AuthRoleDto::getTenantId, tenantId).or().isNull(AuthRoleDto::getTenantId))
            .in(AuthRoleDto::getId, roleIds));
        List<String> filteredRoleIds = roles.stream()
            .map(AuthRoleDto::getId)
            .filter(StringUtils::hasText)
            .distinct()
            .toList();
        if (filteredRoleIds.isEmpty()) {
            return new AuthorizationPermissionSetEntity(List.of(), List.of());
        }

        List<String> permissionIds = rolePermissionMapper.selectList(new LambdaQueryWrapper<AuthRolePermissionDto>()
                .and(wrapper -> wrapper
                    .eq(AuthRolePermissionDto::getTenantId, tenantId)
                    .or()
                    .isNull(AuthRolePermissionDto::getTenantId))
                .in(AuthRolePermissionDto::getRoleId, filteredRoleIds))
            .stream()
            .map(AuthRolePermissionDto::getPermissionId)
            .filter(StringUtils::hasText)
            .distinct()
            .toList();

        List<String> permissionCodes = permissionIds.isEmpty()
            ? List.of()
            : permissionMapper.selectList(new LambdaQueryWrapper<AuthPermissionDto>()
                    .and(wrapper -> wrapper
                        .eq(AuthPermissionDto::getTenantId, tenantId)
                        .or()
                        .isNull(AuthPermissionDto::getTenantId))
                    .in(AuthPermissionDto::getId, permissionIds))
                .stream()
                .map(AuthPermissionDto::getPermissionCode)
                .filter(StringUtils::hasText)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .sorted()
                .toList();

        List<String> roleCodes = roles.stream()
            .map(AuthRoleDto::getRoleCode)
            .filter(StringUtils::hasText)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
            .stream()
            .sorted()
            .toList();
        return new AuthorizationPermissionSetEntity(roleCodes, permissionCodes);
    }

    private Optional<AuthUserDto> selectUser(LambdaQueryWrapper<AuthUserDto> wrapper) {
        return Optional.ofNullable(userMapper.selectOne(wrapper));
    }

    private AuthorizationUserEntity mapUser(AuthUserDto dto) {
        return new AuthorizationUserEntity(
            dto.getId(),
            dto.getTenantId(),
            dto.getKeycloakSubject(),
            dto.getUsername(),
            dto.getDisplayName(),
            dto.getEmail(),
            dto.getDepartmentId()
        );
    }
}
