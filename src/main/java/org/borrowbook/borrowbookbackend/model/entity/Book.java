package org.borrowbook.borrowbookbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
public class Book {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(unique = true)
    private String googleBookId;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String author;
    private String publisher;
}
