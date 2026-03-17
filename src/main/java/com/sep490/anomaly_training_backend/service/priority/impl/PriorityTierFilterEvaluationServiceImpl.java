package com.sep490.anomaly_training_backend.service.priority.impl;

import com.sep490.anomaly_training_backend.enums.FilterLogic;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.PriorityTier;
import com.sep490.anomaly_training_backend.model.PriorityTierFilter;
import com.sep490.anomaly_training_backend.service.priority.ComputedMetricService;
import com.sep490.anomaly_training_backend.service.priority.PriorityTierFilterEvaluationService;
import com.sep490.anomaly_training_backend.util.PriorityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PriorityTierFilterEvaluationServiceImpl implements PriorityTierFilterEvaluationService {

    private final ComputedMetricService metricCalculationService;

    /**
     * Evaluate xem employee có match với tier filters không
     *
     * @param tier            Priority tier có chứa filters
     * @param employee        Employee cần check
     * @param employeeMetrics Map<metricName, value> đã tính sẵn
     * @return true nếu employee match tier filters, false nếu không
     */
    public boolean evaluateTierFilters(PriorityTier tier, Employee employee,
                                       Map<String, Object> employeeMetrics) {
        List<PriorityTierFilter> filters = tier.getFilters();

        if (filters == null || filters.isEmpty()) {
            // Không có filters → mọi employee đều match
            return true;
        }

        // Sắp xếp filters theo filter_order
        List<PriorityTierFilter> sortedFilters = filters.stream()
                .sorted((f1, f2) -> Integer.compare(f1.getFilterOrder(), f2.getFilterOrder()))
                .toList();

        // Kiểm tra filters theo FilterLogic
        if (tier.getFilterLogic() == FilterLogic.AND) {
            // TẤT CẢ filters phải TRUE
            return sortedFilters.stream()
                    .allMatch(filter -> evaluateSingleFilter(filter, employeeMetrics));
        } else {
            // MỘT TRONG các filters phải TRUE
            return sortedFilters.stream()
                    .anyMatch(filter -> evaluateSingleFilter(filter, employeeMetrics));
        }
    }

    /**
     * Evaluate 1 filter
     * <p>
     * Filter format:
     * metric_name: "days_since_last_training"
     * operator: "GT" (>)
     * filter_value: "60"
     * filter_unit: "Ngày"
     */
    private boolean evaluateSingleFilter(PriorityTierFilter filter, Map<String, Object> metricValues) {
        String metricName = filter.getMetricName();
        Object metricValue = metricValues.get(metricName);

        if (metricValue == null) {
            log.warn("Metric not found in calculated values: {}", metricName);
            return false;
        }

        // So sánh metricValue với filter_value theo operator
        return PriorityUtils.compareValue(metricValue, filter.getOperator(), filter.getFilterValue());
    }

    /**
     * Batch evaluate filters cho multiple employees
     *
     * @param tier             Priority tier
     * @param employees        List employees
     * @param employeesMetrics Map<employeeId, Map<metricName, value>>
     * @return Map<employeeId, matchResult>
     */
    public Map<Long, Boolean> batchEvaluateTierFilters(PriorityTier tier, List<Employee> employees,
                                                       Map<Long, Map<String, Object>> employeesMetrics) {
        return employees.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Employee::getId,
                        emp -> evaluateTierFilters(tier, emp, employeesMetrics.get(emp.getId()))
                ));
    }
}
