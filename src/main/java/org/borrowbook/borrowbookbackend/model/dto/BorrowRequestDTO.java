package org.borrowbook.borrowbookbackend.model.dto;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.borrowbook.borrowbookbackend.model.enums.BookRequestStatus;

import java.time.LocalDate;

@Data
public class BorrowRequestDTO {
    @NotNull(message = "Meeting time is required.")
    private LocalDateTime meetingTime;
    @NotNull(message = "Due date is required.")
    private LocalDate dueDate;
    @NotBlank(message = "Location is required.")
    private String location;
}
