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
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_book_id", nullable=false)
    private UserBook userBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="borrower_id", nullable=false)
    private User borrower;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookRequestStatus status;

    private LocalDate borrowed_at;

    @Column(nullable = false)
    private LocalDate due_date;

    private LocalDate returned_at;

    @Column(nullable = false, length=150)
    private String location;
    @Column(nullable = false)
    private LocalDate created_at;
    @Column(nullable = false)
    private LocalDateTime meeting_time;

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
        this.created_at = createdAt;
        this.meeting_time = meetingTime;
        this.due_date = dueDate;
        this.location = location;
    }

}
