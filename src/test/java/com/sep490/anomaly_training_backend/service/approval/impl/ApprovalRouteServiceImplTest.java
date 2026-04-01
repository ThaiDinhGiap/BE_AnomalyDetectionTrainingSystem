package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Permission;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.Section;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalRouteServiceImplTest {

    @Mock
    private GroupRepository groupRepo;

    @InjectMocks
    private ApprovalRouteServiceImpl routeService;

    private Group group;
    private User supervisor;
    private Section section;
    private User manager;

    @BeforeEach
    void setUp() {
        group = new Group();
        group.setId(10L);

        supervisor = new User();
        supervisor.setId(100L);
        supervisor.setUsername("supervisor1");
        
        manager = new User();
        manager.setId(200L);
        manager.setUsername("manager1");

        section = new Section();
        section.setId(5L);
        section.setManager(manager);

        group.setSupervisor(supervisor);
        group.setSection(section);
    }

    private void grantPermission(User user, String permissionName) {
        Role role = new Role();
        Permission p = new Permission();
        p.setPermissionCode(permissionName);
        role.setPermissions(Set.of(p));
        user.setRoles(Set.of(role));
    }

    @Test
    void getApproverForStep_WhenGroupNotFound_ShouldThrowException() {
        when(groupRepo.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routeService.getApproverForStep(10L, "APPROVE_DEFECT"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Group not found");
    }

    @Test
    void getApproverForStep_WhenSupervisorHasPermission_ShouldReturnSupervisor() {
        grantPermission(supervisor, "APPROVE_DEFECT");
        
        when(groupRepo.findById(10L)).thenReturn(Optional.of(group));

        User approver = routeService.getApproverForStep(10L, "APPROVE_DEFECT");

        assertThat(approver).isEqualTo(supervisor);
    }

    @Test
    void getApproverForStep_WhenSupervisorLacksPermissionButManagerHasIt_ShouldReturnManager() {
        // Supervisor has no roles/permissions
        grantPermission(manager, "APPROVE_DEFECT");

        when(groupRepo.findById(10L)).thenReturn(Optional.of(group));

        User approver = routeService.getApproverForStep(10L, "APPROVE_DEFECT");

        assertThat(approver).isEqualTo(manager);
    }

    @Test
    void getApproverForStep_WhenNoOneHasPermission_ShouldThrowException() {
        when(groupRepo.findById(10L)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> routeService.getApproverForStep(10L, "APPROVE_DEFECT"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("No approver found in org hierarchy with permission");
    }

    @Test
    void getApproverIdForStep_ShouldReturnId() {
        grantPermission(supervisor, "APPROVE_DEFECT");
        when(groupRepo.findById(10L)).thenReturn(Optional.of(group));

        Long id = routeService.getApproverIdForStep(10L, "APPROVE_DEFECT");
        assertThat(id).isEqualTo(100L);
    }

    @Test
    void isValidApprover_WhenValid_ShouldReturnTrue() {
        grantPermission(manager, "APPROVE_DEFECT");
        when(groupRepo.findById(10L)).thenReturn(Optional.of(group));

        boolean valid = routeService.isValidApprover(10L, "APPROVE_DEFECT", 200L);
        assertThat(valid).isTrue();
    }

    @Test
    void isValidApprover_WhenInvalidId_ShouldReturnFalse() {
        grantPermission(manager, "APPROVE_DEFECT");
        when(groupRepo.findById(10L)).thenReturn(Optional.of(group));

        boolean valid = routeService.isValidApprover(10L, "APPROVE_DEFECT", 100L); // Expected 200L
        assertThat(valid).isFalse();
    }

    @Test
    void isValidApprover_WhenException_ShouldReturnFalse() {
        when(groupRepo.findById(10L)).thenReturn(Optional.of(group)); // no one has perm -> throws

        boolean valid = routeService.isValidApprover(10L, "APPROVE_DEFECT", 100L);
        assertThat(valid).isFalse();
    }

    @Test
    void resolveExpectedApprover_ShouldReturnOptional() {
        grantPermission(supervisor, "APPROVE_DEFECT");
        when(groupRepo.findByIdAndDeleteFlagFalse(10L)).thenReturn(Optional.of(group));

        Optional<User> expected = routeService.resolveExpectedApprover(10L, "APPROVE_DEFECT");
        assertThat(expected).isPresent().contains(supervisor);
    }
}
