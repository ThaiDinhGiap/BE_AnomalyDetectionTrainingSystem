package com.sep490.anomaly_training_backend.enums;

public enum FilterOperator {
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    EQ("="),
    NEQ("!=");

    private final String symbol;

    FilterOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
