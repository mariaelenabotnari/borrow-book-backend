package org.borrowbook.borrowbookbackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.borrowbook.borrowbookbackend.model.enums.BookRequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowRequestResponseDTO {
    private Integer id;
    private String bookTitle;
    private String borrowerUsername;
    private String ownerUsername;
    private BookRequestStatus status;
    private String location;
    private LocalDateTime meetingTime;
    private LocalDate dueDate;
    private LocalDateTime createdAt;

    public BorrowRequestResponseDTO(BorrowRequest borrowRequest) {
        this.id = borrowRequest.getId();
        this.bookTitle = borrowRequest.getUserBook().getBook().getTitle();
        this.borrowerUsername = borrowRequest.getBorrower().getUsername();
        this.ownerUsername = borrowRequest.getUserBook().getOwner().getUsername();
        this.status = borrowRequest.getStatus();
        this.location = borrowRequest.getLocation();
        this.meetingTime = borrowRequest.getMeetingTime();
        this.dueDate = borrowRequest.getDueDate();
        this.createdAt = borrowRequest.getCreatedAt();
    }
}