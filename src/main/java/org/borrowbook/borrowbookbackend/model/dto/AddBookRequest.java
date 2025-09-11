package org.borrowbook.borrowbookbackend.model.dto;

import lombok.Data;
import org.borrowbook.borrowbookbackend.model.enums.BookStatus;

@Data
public class AddBookRequest {
    private String googleBookId;
    private BookStatus status;
}