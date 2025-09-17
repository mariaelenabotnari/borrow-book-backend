package org.borrowbook.borrowbookbackend.model.dto;
import java.time.LocalDateTime;

import lombok.Data;
import org.borrowbook.borrowbookbackend.model.enums.BookRequestStatus;

import java.time.LocalDate;

@Data
public class BorrowRequestDTO {
    private LocalDateTime meeting_time;
    private LocalDate due_date;
    private String location;
}
