package de.langen.beschlussservice.domain.entity;

import lombok.Getter;

@Getter
public enum DecisionStatus {
    PENDING("pending"),
    IN_PROGRESS("in-progress"),
    COMPLETED("completed");

    private final String value;

    DecisionStatus(String value) {
        this.value = value;
    }

}

