package com.sep490.anomaly_training_backend.util;

import com.sep490.anomaly_training_backend.enums.FilterOperator;
import com.sep490.anomaly_training_backend.enums.MetricReturnType;

import java.math.BigDecimal;

public class PriorityUtils {
    public static Object convertValue(Object value, MetricReturnType targetType) {
        if (value == null) return null;

        switch (targetType) {
            case INT:
                if (value instanceof Integer) return value;
                if (value instanceof Number) return ((Number) value).intValue();
                return Integer.parseInt(value.toString());

            case DECIMAL:
                if (value instanceof BigDecimal) return value;
                if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
                return new BigDecimal(value.toString());

            case BOOLEAN:
                if (value instanceof Boolean) return value;
                return Boolean.parseBoolean(value.toString());

            case STRING:
                return value.toString();

            default:
                return value;
        }
    }

    public static boolean compareValue(Object value, FilterOperator operator, String compareValue) {
        if (value == null) return false;

        try {
            return switch (operator) {
                case GT -> // >
                        compareNumbers(value, compareValue) > 0;
                case GTE -> // >=
                        compareNumbers(value, compareValue) >= 0;
                case LT -> // <
                        compareNumbers(value, compareValue) < 0;
                case LTE -> // <=
                        compareNumbers(value, compareValue) <= 0;
                case EQ -> // =
                        value.toString().equals(compareValue);
                case NEQ -> // !=
                        !value.toString().equals(compareValue);
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }

    private static int compareNumbers(Object value, String compareValue) {
        BigDecimal val1 = new BigDecimal(value.toString());
        BigDecimal val2 = new BigDecimal(compareValue);
        return val1.compareTo(val2);
    }
}
