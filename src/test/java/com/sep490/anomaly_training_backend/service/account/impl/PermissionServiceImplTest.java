package com.sep490.anomaly_training_backend.service.account.impl;

import com.sep490.anomaly_training_backend.dto.response.ModulePermissionResponse;
import com.sep490.anomaly_training_backend.dto.response.UserPermissionResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Module;
import com.sep490.anomaly_training_backend.model.Permission;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ModuleRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private User mockUser;
    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        Module adminModule = new Module();
        adminModule.setModuleCode("admin_module");

        Permission perm1 = new Permission();
        perm1.setPermissionCode("VIEW_USERS");
        perm1.setModule(adminModule);

        Permission perm2 = new Permission();
        perm2.setPermissionCode("EDIT_USERS");
        perm2.setModule(adminModule);

        // Role 1 is active with permissions
        Role activeRole = new Role();
        activeRole.setIsActive(true);
        activeRole.setDeleteFlag(false);
        activeRole.setPermissions(Set.of(perm1, perm2));

        // Role 2 is inactive with permissions (should be ignored)
        Role inactiveRole = new Role();
        inactiveRole.setIsActive(false);
        inactiveRole.setDeleteFlag(false);
        Permission perm3 = new Permission();
        perm3.setPermissionCode("DELETE_USERS");
        inactiveRole.setPermissions(Set.of(perm3));

        mockUser.setRoles(Set.of(activeRole, inactiveRole));
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityUtils != null) {
            mockedSecurityUtils.close();
        }
    }

    @Test
    void getUserPermissions_ShouldReturnMapSuccessfully() {
        when(userRepository.findByIdWithRolesAndPermissions(1L)).thenReturn(Optional.of(mockUser));

        Map<String, List<String>> permissions = permissionService.getUserPermissions(1L);

        assertThat(permissions).containsKey("admin_module");
        assertThat(permissions.get("admin_module")).containsExactlyInAnyOrder("VIEW_USERS", "EDIT_USERS");
        assertThat(permissions.values().stream().flatMap(List::stream)).doesNotContain("DELETE_USERS");
    }

    @Test
    void getUserPermissions_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findByIdWithRolesAndPermissions(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.getUserPermissions(99L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void hasPermission_WhenUserHasPermission_ShouldReturnTrue() {
        when(userRepository.findByIdWithRolesAndPermissions(1L)).thenReturn(Optional.of(mockUser));

        boolean hasPerm = permissionService.hasPermission(1L, "VIEW_USERS");

        assertThat(hasPerm).isTrue();
    }

    @Test
    void hasPermission_WhenUserLacksPermission_ShouldReturnFalse() {
        when(userRepository.findByIdWithRolesAndPermissions(1L)).thenReturn(Optional.of(mockUser));

        boolean hasPerm = permissionService.hasPermission(1L, "NON_EXISTENT_PERM");

        assertThat(hasPerm).isFalse();
    }

    @Test
    void getCurrentUserPermissions_ShouldReturnMapForCurrentUser() {
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(mockUser);

        Map<String, List<String>> permissions = permissionService.getCurrentUserPermissions();

        assertThat(permissions).containsKey("admin_module");
        assertThat(permissions.get("admin_module")).contains("VIEW_USERS");
    }

    @Test
    void getAllModulesWithPermissions_ShouldReturnList() {
        Module mod = new Module();
        mod.setId(1L);
        mod.setModuleCode("test_module");
        mod.setDisplayName("Test Module");

        Permission p = new Permission();
        p.setId(10L);
        p.setPermissionCode("TEST_PERM");
        mod.setPermissions(List.of(p));

        when(moduleRepository.findAll()).thenReturn(List.of(mod));

        List<ModulePermissionResponse> response = permissionService.getAllModulesWithPermissions();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getModuleCode()).isEqualTo("test_module");
        assertThat(response.get(0).getPermissions()).hasSize(1);
        assertThat(response.get(0).getPermissions().get(0).getPermissionCode()).isEqualTo("TEST_PERM");
    }

    @Test
    void getUserPermissionDetail_ShouldReturnUserDetailsWithPermissions() {
        when(userRepository.findByIdWithRolesAndPermissions(1L)).thenReturn(Optional.of(mockUser));

        UserPermissionResponse response = permissionService.getUserPermissionDetail(1L);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRoles()).hasSize(2); // Only non-deleted roles mapped (we set deleteFlag=false for both)
        assertThat(response.getPermissionsByModule()).containsKey("admin_module");
    }
}
