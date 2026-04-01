package com.sep490.anomaly_training_backend.service.account.impl;

import com.sep490.anomaly_training_backend.dto.request.RolePermissionRequest;
import com.sep490.anomaly_training_backend.dto.request.RoleRequest;
import com.sep490.anomaly_training_backend.dto.request.UserRoleRequest;
import com.sep490.anomaly_training_backend.dto.response.PermissionResponse;
import com.sep490.anomaly_training_backend.dto.response.RoleDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.RoleResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Permission;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.PermissionRepository;
import com.sep490.anomaly_training_backend.repository.RoleRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role mockRole;
    private Permission mockPermission;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockRole = new Role();
        mockRole.setId(1L);
        mockRole.setRoleCode("ROLE_CUSTOM");
        mockRole.setDisplayName("Custom Role");
        mockRole.setIsSystem(false);
        mockRole.setIsActive(true);
        mockRole.setDeleteFlag(false);
        mockRole.setPermissions(new HashSet<>());

        mockPermission = new Permission();
        mockPermission.setId(10L);
        mockPermission.setPermissionCode("VIEW_DATA");

        mockUser = new User();
        mockUser.setId(100L);
        mockUser.setRoles(new HashSet<>());
    }

    @Test
    void getAllRoles_ShouldReturnList() {
        when(roleRepository.findByDeleteFlagFalse()).thenReturn(List.of(mockRole));

        List<RoleResponse> result = roleService.getAllRoles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoleCode()).isEqualTo("ROLE_CUSTOM");
    }

    @Test
    void getRoleById_ShouldReturnDetail() {
        mockRole.getPermissions().add(mockPermission);
        when(roleRepository.findByIdWithPermissions(1L)).thenReturn(Optional.of(mockRole));

        RoleDetailResponse result = roleService.getRoleById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPermissions()).hasSize(1);
        assertThat(result.getPermissions().get(0).getPermissionCode()).isEqualTo("VIEW_DATA");
    }

    @Test
    void createRole_ShouldSaveAndReturn() {
        RoleRequest request = new RoleRequest();
        request.setRoleCode("ROLE_NEW");
        request.setDisplayName("New Role");

        when(roleRepository.existsByRoleCode("ROLE_NEW")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role r = invocation.getArgument(0);
            r.setId(2L);
            return r;
        });

        RoleResponse result = roleService.createRole(request);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getRoleCode()).isEqualTo("ROLE_NEW");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void updateRole_ShouldSaveAndReturn() {
        RoleRequest request = new RoleRequest();
        request.setRoleCode("ROLE_MODIFIED");
        
        when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
        when(roleRepository.existsByRoleCode("ROLE_MODIFIED")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(mockRole);

        RoleResponse result = roleService.updateRole(1L, request);

        assertThat(result.getRoleCode()).isEqualTo("ROLE_MODIFIED");
    }

    @Test
    void updateRole_SystemRole_ShouldThrowException() {
        mockRole.setIsSystem(true);
        RoleRequest request = new RoleRequest();
        
        when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));

        assertThatThrownBy(() -> roleService.updateRole(1L, request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ErrorCode.SYSTEM_ROLE_MODIFICATION_NOT_ALLOWED.getMessage());
    }

    @Test
    void deleteRole_ShouldSetDeleteFlag() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
        when(roleRepository.save(any(Role.class))).thenReturn(mockRole);

        roleService.deleteRole(1L);

        assertThat(mockRole.isDeleteFlag()).isTrue();
    }

    @Test
    void assignPermissions_ShouldUpdateRolePermissions() {
        RolePermissionRequest request = new RolePermissionRequest();
        request.setPermissionIds(Set.of(10L));

        when(roleRepository.findByIdWithPermissions(1L)).thenReturn(Optional.of(mockRole));
        when(permissionRepository.findByIdInAndDeleteFlagFalse(Set.of(10L))).thenReturn(List.of(mockPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(mockRole);

        RoleDetailResponse result = roleService.assignPermissions(1L, request);

        assertThat(result.getPermissions()).hasSize(1);
    }

    @Test
    void getRolePermissions_ShouldReturnPermissions() {
        mockRole.getPermissions().add(mockPermission);
        when(roleRepository.findByIdWithPermissions(1L)).thenReturn(Optional.of(mockRole));

        List<PermissionResponse> result = roleService.getRolePermissions(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPermissionCode()).isEqualTo("VIEW_DATA");
    }

    @Test
    void getUserRoles_ShouldReturnRoles() {
        mockUser.getRoles().add(mockRole);
        when(userRepository.findByIdWithRolesAndPermissions(100L)).thenReturn(Optional.of(mockUser));

        List<RoleResponse> result = roleService.getUserRoles(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoleCode()).isEqualTo("ROLE_CUSTOM");
    }

    @Test
    void assignRoles_ShouldUpdateUserRoles() {
        UserRoleRequest request = new UserRoleRequest();
        request.setRoleIds(Set.of(1L));

        when(userRepository.findByIdWithRolesAndPermissions(100L)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findByDeleteFlagFalse()).thenReturn(List.of(mockRole));

        List<RoleResponse> result = roleService.assignRoles(100L, request);

        assertThat(result).hasSize(1);
        verify(userRepository).save(mockUser);
        assertThat(mockUser.getRoles()).contains(mockRole);
    }

    @Test
    void assignRoles_InvalidRoleIds_ShouldThrowException() {
        UserRoleRequest request = new UserRoleRequest();
        request.setRoleIds(Set.of(1L, 99L));

        when(userRepository.findByIdWithRolesAndPermissions(100L)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findByDeleteFlagFalse()).thenReturn(List.of(mockRole));

        assertThatThrownBy(() -> roleService.assignRoles(100L, request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ErrorCode.INVALID_ROLE_IDS.getMessage());
    }
}
