package de.langen.decision_service.domain.entity;

import lombok.Getter;

/**
 * Priority levels for decisions.
 * Used to indicate urgency and importance of implementation.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Getter
public enum DecisionPriority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    URGENT("urgent");

    private final String value;

    DecisionPriority(String value) {
        this.value = value;
    }

    /**
     * Parse priority from string value
     * @param value string value (case-insensitive)
     * @return corresponding priority
     * @throws IllegalArgumentException if value is invalid
     */
    public static DecisionPriority fromValue(String value) {
        if (value == null || value.isBlank()) {
            return MEDIUM; // Default
        }

        String normalized = value.toUpperCase().replace("-", "_");
        try {
            return DecisionPriority.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid priority value: " + value +
                            ". Must be one of: low, medium, high, urgent"
            );
        }
    }

    /**
     * Check if this priority is high (HIGH or URGENT)
     * @return true if high priority
     */
    public boolean isHigh() {
        return this == HIGH || this == URGENT;
    }

    /**
     * Check if this priority is low (LOW or MEDIUM)
     * @return true if low priority
     */
    public boolean isLow() {
        return this == LOW || this == MEDIUM;
    }
}