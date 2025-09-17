package org.borrowbook.borrowbookbackend.model.dto;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.borrowbook.borrowbookbackend.model.enums.BookRequestStatus;

import java.time.LocalDate;

@Data
public class BorrowRequestDTO {
    @NotNull(message = "Meeting time is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime meetingTime;

    @NotNull(message = "Location is required")
    private String location;
}
