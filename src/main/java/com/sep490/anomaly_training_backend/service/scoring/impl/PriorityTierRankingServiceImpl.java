package com.sep490.anomaly_training_backend.service.scoring.impl;

import com.sep490.anomaly_training_backend.enums.RankingDirection;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.PriorityTier;
import com.sep490.anomaly_training_backend.service.scoring.PriorityTierRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriorityTierRankingServiceImpl implements PriorityTierRankingService {
    /**
     * Sắp xếp employees theo ranking metric của tier
     *
     * @param tier             Priority tier (chứa ranking_metric, secondary_metric)
     * @param employees        List employees cần sắp xếp
     * @param employeesMetrics Map<employeeId, Map<metricName, value>>
     * @return List employees đã sắp xếp + rank info
     */
    public List<EmployeeRankingResult> rankEmployees(PriorityTier tier, List<Employee> employees,
                                                     Map<Long, Map<String, Object>> employeesMetrics) {
        // Tạo comparator dựa trên ranking_metric + secondary_metric
        Comparator<Employee> comparator = createComparator(tier, employeesMetrics);

        // Sắp xếp employees
        List<Employee> sortedEmployees = employees.stream()
                .sorted(comparator)
                .toList();

        // Tạo ranking results với sort_rank
        return sortedEmployees.stream()
                .map((emp) -> new EmployeeRankingResult(
                        emp,
                        employeesMetrics.get(emp.getId()),
                        sortedEmployees.indexOf(emp) + 1  // 1-based rank
                ))
                .toList();
    }

    /**
     * Tạo comparator để sắp xếp employees
     */
    private Comparator<Employee> createComparator(PriorityTier tier, Map<Long, Map<String, Object>> employeesMetrics) {
        String rankingMetric = tier.getRankingMetric();
        RankingDirection rankingDir = tier.getRankingDirection();
        String secondaryMetric = tier.getSecondaryMetric();
        RankingDirection secondaryDir = tier.getSecondaryDirection();

        // Primary comparator
        Comparator<Employee> comparator = (emp1, emp2) -> {
            Object val1 = employeesMetrics.get(emp1.getId()).get(rankingMetric);
            Object val2 = employeesMetrics.get(emp2.getId()).get(rankingMetric);

            int cmp = compareValues(val1, val2);
            return rankingDir == RankingDirection.DESC ? -cmp : cmp;
        };

        // Add secondary comparator nếu có
        if (secondaryMetric != null && !secondaryMetric.isBlank()) {
            comparator = comparator.thenComparing((emp1, emp2) -> {
                Object val1 = employeesMetrics.get(emp1.getId()).get(secondaryMetric);
                Object val2 = employeesMetrics.get(emp2.getId()).get(secondaryMetric);

                int cmp = compareValues(val1, val2);
                return secondaryDir == RankingDirection.DESC ? -cmp : cmp;
            });
        }

        return comparator;
    }

    /**
     * So sánh 2 giá trị
     */
    private int compareValues(Object val1, Object val2) {
        if (val1 == null && val2 == null) return 0;
        if (val1 == null) return -1;
        if (val2 == null) return 1;

        // Cố gắng convert thành số để so sánh
        try {
            if (val1 instanceof Number && val2 instanceof Number) {
                BigDecimal num1 = new BigDecimal(val1.toString());
                BigDecimal num2 = new BigDecimal(val2.toString());
                return num1.compareTo(num2);
            }

            // Compare as string
            return val1.toString().compareTo(val2.toString());
        } catch (Exception e) {
            log.error("Error comparing values: {} and {}", val1, val2, e);
            return 0;
        }
    }

}
