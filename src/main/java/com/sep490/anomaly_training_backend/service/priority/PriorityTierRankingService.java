package com.sep490.anomaly_training_backend.service.priority;

import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.PriorityTier;

import java.util.List;
import java.util.Map;

public interface PriorityTierRankingService {
    List<EmployeeRankingResult> rankEmployees(PriorityTier tier, List<Employee> employees, Map<Long, Map<String, Object>> employeesMetrics);

    public static class EmployeeRankingResult {
        public final Employee employee;
        public final Map<String, Object> metricValues;
        public final int sortRank;

        public EmployeeRankingResult(Employee employee, Map<String, Object> metricValues, int sortRank) {
            this.employee = employee;
            this.metricValues = metricValues;
            this.sortRank = sortRank;
        }

        public Long getEmployeeId() {
            return employee.getId();
        }

        public String getEmployeeCode() {
            return employee.getEmployeeCode();
        }

        public String getFullName() {
            return employee.getFullName();
        }
    }
}
