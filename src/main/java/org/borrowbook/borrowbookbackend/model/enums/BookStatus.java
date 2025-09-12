package org.borrowbook.borrowbookbackend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BookStatus {
    AVAILABLE,
    BORROWED;

    @JsonCreator
    public static BookStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return BookStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid BookStatus: " + value + ". Valid values are: AVAILABLE, BORROWED");
        }
    }

    @JsonValue
    public String toString() {
        return this.name();
    }
}