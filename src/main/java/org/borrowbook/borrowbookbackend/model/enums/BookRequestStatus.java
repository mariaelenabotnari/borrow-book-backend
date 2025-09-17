package org.borrowbook.borrowbookbackend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BookRequestStatus {
    PENDING, ACCEPTED, REJECTED;

    @JsonCreator
    public static BookRequestStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return BookRequestStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid BookRequestStatus: " + value + ". Valid values are: PENDING, ACCEPTED, REJECTED");
        }
    }

    @JsonValue
    public String toString() {
        return this.name();
    }
}
