package org.borrowbook.borrowbookbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.borrowbook.borrowbookbackend.model.enums.BookRequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "borrow_request")
public class BorrowRequest {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="userBookId", nullable=false)
    private UserBook userBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="borrowerId", nullable=false)
    private User borrower;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookRequestStatus status;

    private LocalDate borrowedAt;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate returnedAt;

    @Column(nullable = false, length=150)
    private String location;
    @Column(nullable = false)
    private LocalDate createdAt;
    @Column(nullable = false)
    private LocalDateTime meetingTime;

    public BorrowRequest(
            UserBook userBook,
            User borrower,
            BookRequestStatus status,
            LocalDate createdAt,
            LocalDateTime meetingTime,
            LocalDate dueDate,
            String location
    ) {
        this.userBook = userBook;
        this.borrower = borrower;
        this.status = status;
        this.createdAt = createdAt;
        this.meetingTime = meetingTime;
        this.dueDate = dueDate;
        this.location = location;
    }

}
