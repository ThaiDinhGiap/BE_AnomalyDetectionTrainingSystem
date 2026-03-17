package com.sep490.anomaly_training_backend.service.priority.impl;

import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.MetricClassification;
import com.sep490.anomaly_training_backend.repository.MetricClassificationRepository;
import com.sep490.anomaly_training_backend.service.priority.MetricClassificationService;
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
public class MetricClassificationServiceImpl implements MetricClassificationService {

    private final MetricClassificationRepository classificationRepository;

    /**
     * Classify 1 metric value theo classification rules
     *
     * @param classificationName Tên classification (VD: "training_priority")
     * @param metricValue        Giá trị metric cần classify
     * @return Map {level, label} hoặc null nếu không match
     */
    public Map<String, Object> classifyMetric(String classificationName, Object metricValue) {
        List<MetricClassification> rules = classificationRepository
                .findByClassificationNameAndIsActiveTrueOrderByPriority(classificationName);

        if (rules.isEmpty()) {
            throw new AppException(ErrorCode.CLASSIFICATION_NOT_FOUND,
                    "Classification rules not found: " + classificationName);
        }

        // Tìm rule matching với metricValue
        for (MetricClassification rule : rules) {
            if (evaluateCondition(rule.getConditionExpression(), metricValue)) {
                return Map.of(
                        "level", rule.getOutputLevel(),
                        "label", rule.getOutputLabel() != null ? rule.getOutputLabel() : "",
                        "priority", rule.getPriority()
                );
            }
        }

        log.warn("No matching classification rule for: {} with value: {}", classificationName, metricValue);
        return null;
    }

    /**
     * Classify multiple metrics (batch)
     *
     * @param classificationName Tên classification
     * @param metricValues       List giá trị cần classify
     * @return List Map {level, label}
     */
    public List<Map<String, Object>> classifyBatch(String classificationName, List<Object> metricValues) {
        return metricValues.stream()
                .map(value -> classifyMetric(classificationName, value))
                .toList();
    }

    /**
     * Đánh giá condition expression
     * <p>
     * Support format: "value > 60", "value <= 30", "value == TRUE", etc.
     *
     * @param condition   VD: "value > 60"
     * @param actualValue Giá trị thực tế
     */
    private boolean evaluateCondition(String condition, Object actualValue) {
        try {
            // Parse condition: "value OPERATOR number"
            // VD: "value > 60" → operator = ">", number = 60

            condition = condition.trim();
            String[] parts = condition.split("\\s+");

            if (parts.length < 3 || !"value".equals(parts[0])) {
                throw new AppException(ErrorCode.INVALID_CLASSIFICATION_RULE,
                        "Invalid condition format: " + condition);
            }

            String operator = parts[1];
            String compareValueStr = parts[2];

            // So sánh giá trị
            return switch (operator) {
                case ">" -> compareAsNumbers(actualValue, compareValueStr) > 0;
                case ">=" -> compareAsNumbers(actualValue, compareValueStr) >= 0;
                case "<" -> compareAsNumbers(actualValue, compareValueStr) < 0;
                case "<=" -> compareAsNumbers(actualValue, compareValueStr) <= 0;
                case "==" -> actualValue.toString().equals(compareValueStr);
                case "!=" -> !actualValue.toString().equals(compareValueStr);
                default -> throw new AppException(ErrorCode.INVALID_CLASSIFICATION_RULE,
                        "Unknown operator: " + operator);
            };
        } catch (Exception e) {
            log.error("Error evaluating condition: {} for value: {}", condition, actualValue, e);
            return false;
        }
    }

    /**
     * So sánh 2 giá trị dưới dạng số
     */
    private int compareAsNumbers(Object val1, String val2Str) {
        try {
            double num1 = Double.parseDouble(val1.toString());
            double num2 = Double.parseDouble(val2Str);
            return Double.compare(num1, num2);
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.INVALID_CLASSIFICATION_VALUE,
                    "Cannot compare non-numeric values");
        }
    }
}
