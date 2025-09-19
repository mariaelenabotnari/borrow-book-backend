package org.borrowbook.borrowbookbackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.borrowbook.borrowbookbackend.model.entity.Book;
import org.borrowbook.borrowbookbackend.model.entity.UserBook;
import org.borrowbook.borrowbookbackend.model.enums.BookStatus;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionBookDTO {
    private Integer userBookId;
    private String title;
    private List<String> authors;
    private String imageLink;
    private BookStatus status;
    private String username;
    private boolean isPending;

    public CollectionBookDTO(UserBook userBook) {
        this.userBookId = userBook.getId();
        this.title = userBook.getBook().getTitle();
        this.authors = userBook.getBook().getAuthor();
        this.imageLink = userBook.getBook().getImageLink();
        this.status = userBook.getStatus();
        this.username = userBook.getOwner().getUsername();
        this.isPending = false;
    }

    public CollectionBookDTO(UserBook userBook, boolean isPending) {
        this.userBookId = userBook.getId();
        this.title = userBook.getBook().getTitle();
        this.authors = userBook.getBook().getAuthor();
        this.imageLink = userBook.getBook().getImageLink();
        this.status = userBook.getStatus();
        this.username = userBook.getOwner().getUsername();
        this.isPending = isPending;
    }
}