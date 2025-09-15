package org.borrowbook.borrowbookbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.borrowbook.borrowbookbackend.model.enums.BookStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_book")
public class UserBook {
    @Id
    @GeneratedValue
    Integer id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ownerId", nullable=false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookId", nullable = false)
    private Book book;

    public UserBook(BookStatus status, User user, Book book){
        this.status = status;
        this.owner = user;
        this.book = book;
    }
}
