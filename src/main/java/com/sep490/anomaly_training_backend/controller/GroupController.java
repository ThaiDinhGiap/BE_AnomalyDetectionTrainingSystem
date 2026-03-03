package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
@Tag(name = "Group Management", description = "API for managing production groups/teams")
public class GroupController {
    private final GroupService groupService;

    @Operation(summary = "Get groups by Team Leader ID")
    @GetMapping("/team-lead/{id}")
    @PreAuthorize("hasAnyAuthority('group.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getByTeamLead(@PathVariable Long id) {
        List<GroupResponse> results = groupService.getGroupByTeamLead(id);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @Operation(summary = "Get groups by Supervisor ID")
    @GetMapping("/supervisor/{id}")
    @PreAuthorize("hasAnyAuthority('group.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getBySupervisor(@PathVariable Long id) {
        List<GroupResponse> results = groupService.getGroupsBySupervisor(id);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
