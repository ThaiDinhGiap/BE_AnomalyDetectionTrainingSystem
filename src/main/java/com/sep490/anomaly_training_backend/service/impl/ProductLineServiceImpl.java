package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ProductLineRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.mapper.EmployeeSkillMapper;
import com.sep490.anomaly_training_backend.mapper.ProductLineMapper;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.service.ProductLineService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductLineServiceImpl implements ProductLineService {
    private final ProductLineRepository productLineRepository;
    private final ProductLineMapper productLineMapper;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeSkillMapper employeeSkillMapper;
    private final GroupRepository groupRepository;
    @Override
    public List<ProductLineResponse> getAllProductLine() {
        List<ProductLineResponse> responses = productLineRepository.findByDeleteFlagFalse()
                                                                   .stream().map(productLineMapper::toDto)
                                                                   .toList();
        for (ProductLineResponse response : responses) {
            for (ProcessResponse processResponse : response.getProcesses()) {
                List<EmployeeSkillResponse> skillResponses = employeeSkillRepository.findByProcessIdAndDeleteFlagFalse(processResponse.getId())
                                                                                    .stream().map(employeeSkillMapper::toDto).toList();
                processResponse.setSkillsProcess(skillResponses);
            }
        }
        return responses;
    }

    @Override
    public ProductLineResponse createProductLine(ProductLineRequest request) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Group not found with id: " + request.getGroupId()
                ));

        ProductLine productLine = ProductLine.builder()
                .name(request.getName())
                .group(group)
                .build();

        return productLineMapper.toDto(productLine);
    }

    @Override
    public void deleteProductLine(Long id) {
        ProductLine productLine = productLineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Group not found with id: " + id
                ));
        productLine.setDeleteFlag(true);
        productLineRepository.save(productLine);
    }

    @Override
    public ProductLineResponse updateProductLine(Long id, ProductLineRequest productLineRequest) {
        ProductLine productLine = productLineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Group not found with id: " + id
                ));
        return productLineMapper.toDto(productLineRepository.save(productLine));
    }

    @Override
    public List<ProductLineResponse> getByTeamLeadId(Long teamLeadId) {
        return productLineRepository.findProductLineByTeamLeadId(teamLeadId).
                                    stream()
                                    .map(productLineMapper::toDto)
                                    .toList();
    }
}
