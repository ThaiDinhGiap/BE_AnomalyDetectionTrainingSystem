package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.GroupRequest;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.mapper.GroupMapper;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import com.sep490.anomaly_training_backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;

    @Override
    @Transactional
    public GroupResponse createGroup(GroupRequest request) {
        // Kiểm tra tên trùng (nếu cần)
        if (groupRepository.existsByName(request.getName())) {
            // Lưu ý: Logic này có thể chặt quá nếu cho phép trùng tên khác Section
            // Nếu muốn cho trùng tên ở Section khác nhau thì dùng existsByNameAndSectionId
            throw new RuntimeException("Tên nhóm đã tồn tại");
        }

        // Mapper tự tìm Section và Supervisor
        Group group = groupMapper.toEntity(request);
        return groupMapper.toDTO(groupRepository.save(group));
    }

    @Override
    @Transactional
    public GroupResponse updateGroup(Long id, GroupRequest request) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found id: " + id));

        groupMapper.updateEntity(group, request);
        return groupMapper.toDTO(groupRepository.save(group));
    }

    @Override
    @Transactional
    public void deleteGroup(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found id: " + id));

        // Soft delete
        group.setDeleteFlag(true);
        groupRepository.save(group);
    }

    @Override
    public GroupResponse getGroupById(Long id) {
        return groupRepository.findById(id)
                .filter(g -> !g.isDeleteFlag())
                .map(groupMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Group not found id: " + id));
    }

    @Override
    public List<GroupResponse> getAllGroups() {
        return groupRepository.findAll().stream()
                .filter(g -> !g.isDeleteFlag())
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupResponse> getGroupsBySection(Long sectionId) {
        return groupRepository.findBySectionId(sectionId).stream()
                .filter(g -> !g.isDeleteFlag())
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupResponse> getGroupByTeamLead(Long teamLeadId) {
        return groupRepository.findByTeamLeadId(teamLeadId).stream().map(groupMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<GroupResponse> getGroupsBySupervisor(Long supervisorId) {
        return groupRepository.findBySupervisorId(supervisorId).stream().map(groupMapper::toDTO).collect(Collectors.toList());
    }
}