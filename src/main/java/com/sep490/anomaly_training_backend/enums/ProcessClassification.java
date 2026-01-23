package com.sep490.anomaly_training_backend.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum ProcessClassification {
    C1(1), C2(2), C3(3), C4(4);

    private final int value;

    ProcessClassification(int value) { this.value = value; }

    public int getValue() { return value; }

    public static ProcessClassification fromValue(int v) {
        for (ProcessClassification pc : values()) if (pc.value == v) return pc;
        return C4;
    }

    @Converter(autoApply = true)
    public static class ConverterImpl implements AttributeConverter<ProcessClassification, Integer> {
        @Override
        public Integer convertToDatabaseColumn(ProcessClassification attribute) {
            return attribute == null ? null : attribute.getValue();
        }

        @Override
        public ProcessClassification convertToEntityAttribute(Integer dbData) {
            return dbData == null ? null : ProcessClassification.fromValue(dbData);
        }
    }
}
