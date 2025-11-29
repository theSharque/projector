package com.projector.feature.model;

public enum Quarter {
    Q1("Q1"),
    Q2("Q2"),
    Q3("Q3"),
    Q4("Q4");

    private final String value;

    Quarter(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Quarter fromValue(String value) {
        for (Quarter quarter : values()) {
            if (quarter.value.equals(value)) {
                return quarter;
            }
        }
        throw new IllegalArgumentException("Unknown quarter: " + value);
    }
}

