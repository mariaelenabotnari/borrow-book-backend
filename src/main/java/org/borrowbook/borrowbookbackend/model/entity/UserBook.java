package org.borrowbook.borrowbookbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    String status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ownerId", nullable=false)
    private User owner;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookId", nullable = false)
    private Book book;
}
