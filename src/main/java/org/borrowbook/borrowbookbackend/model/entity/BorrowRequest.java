package org.borrowbook.borrowbookbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="borrower_id", nullable=false)
    UserBook borrower;

    @Column(nullable = false)
    String status;

    LocalDate borrowed_at;
    LocalDate due_date;
    LocalDate returned_at;

}
