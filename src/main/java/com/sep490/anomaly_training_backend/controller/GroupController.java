package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupController {
    private final GroupService groupService;

    @GetMapping("/team-lead/{id}")
    public ResponseEntity<List<GroupResponse>> getByTeamLead(@PathVariable Long id) {
        List<GroupResponse> results = groupService.getGroupByTeamLead(id);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/supervisor/{id}")
    public ResponseEntity<List<GroupResponse>> getBySupervisor(@PathVariable Long id) {
        List<GroupResponse> results = groupService.getGroupsBySupervisor(id);
        return ResponseEntity.ok(results);
    }
}
