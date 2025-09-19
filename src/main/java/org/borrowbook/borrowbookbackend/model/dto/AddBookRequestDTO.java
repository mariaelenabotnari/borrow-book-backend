package org.borrowbook.borrowbookbackend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.borrowbook.borrowbookbackend.model.enums.BookStatus;

@Data
public class AddBookRequestDTO {
    @NotBlank(message = "Google Book ID is required")
    private String googleBookId;
    @NotBlank(message = "Status is required")
    private BookStatus status;
}