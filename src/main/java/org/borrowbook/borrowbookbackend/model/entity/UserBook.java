package org.borrowbook.borrowbookbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.borrowbook.borrowbookbackend.model.enums.BookStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_book")
public class UserBook {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ownerId", nullable=false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookId", nullable = false)
    private Book book;

    @OneToMany(mappedBy = "userBook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BorrowRequest> borrowRequests;

    public UserBook(BookStatus status, User user, Book book){
        this.status = status;
        this.owner = user;
        this.book = book;
    }
}