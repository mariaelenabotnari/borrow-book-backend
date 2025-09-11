package org.borrowbook.borrowbookbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(unique = true)
    private String googleBookId;
    @Column(nullable = false)
    private String title;
    @ElementCollection
    @CollectionTable(name = "book_author", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "author")
    private List<String> author;
    private String publisher;
    private String imageLink;
}
