package de.langen.decision_service.domain.entity;

import lombok.Getter;

/**
 * Status values for reports indicating their approval workflow state.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Getter
public enum ReportStatus {
    DRAFT("draft"),
    SUBMITTED("submitted"),
    APPROVED("approved"),
    REJECTED("rejected");

    private final String value;

    ReportStatus(String value) {
        this.value = value;
    }

    /**
     * Parse status from string value
     * @param value string value (case-insensitive)
     * @return corresponding status
     * @throws IllegalArgumentException if value is invalid
     */
    public static ReportStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return DRAFT; // Default
        }

        String normalized = value.toUpperCase().replace("-", "_");
        try {
            return ReportStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid report status value: " + value +
                            ". Must be one of: draft, submitted, approved, rejected"
            );
        }
    }

    /**
     * Check if status allows editing
     * @return true if status is DRAFT or REJECTED
     */
    public boolean isEditable() {
        return this == DRAFT || this == REJECTED;
    }

    /**
     * Check if status is final (approved or rejected)
     * @return true if final
     */
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED;
    }
}