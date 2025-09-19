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
public class UserBooksDTO {
    private Integer userBookId;
    private String title;
    private List<String> authors;
    private String imageLink;
    private BookStatus status;
    private boolean isPending;

    public UserBooksDTO(UserBook userBook, Book book) {
        this.userBookId = userBook.getId();
        this.title = book.getTitle();
        this.authors = book.getAuthor();
        this.imageLink = book.getImageLink();
        this.status = userBook.getStatus();
        this.isPending = false;
    }

    public UserBooksDTO(UserBook userBook, Book book, boolean isPending) {
        this.userBookId = userBook.getId();
        this.title = book.getTitle();
        this.authors = book.getAuthor();
        this.imageLink = book.getImageLink();
        this.status = userBook.getStatus();
        this.isPending = isPending;
    }
}