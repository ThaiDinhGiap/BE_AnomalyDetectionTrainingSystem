package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.GroupRequest;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;

import java.util.List;

public interface GroupService {
    GroupResponse createGroup(GroupRequest request);

    GroupResponse updateGroup(Long id, GroupRequest request);

    void deleteGroup(Long id);

    void removeGroupFromSection(Long id);

    GroupResponse getGroupById(Long id);

    List<GroupResponse> getAllGroups();

    List<GroupResponse> getMyManagedGroups();

    List<GroupResponse> getGroupByTeamLead(Long teamLeadId);

    List<GroupResponse> getGroupsBySupervisor(Long teamId);
}