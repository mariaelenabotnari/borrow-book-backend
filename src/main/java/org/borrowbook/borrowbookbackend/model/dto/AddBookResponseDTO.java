package org.borrowbook.borrowbookbackend.model.dto;

import lombok.Data;
import org.borrowbook.borrowbookbackend.model.entity.UserBook;
import org.borrowbook.borrowbookbackend.model.enums.BookStatus;

import java.util.List;

@Data
public class AddBookResponseDTO {
    private Integer userBookId;
    private BookStatus status;
    private Integer ownerId;
    private Integer bookId;
    private String title;
    private List<String> author;
    private String publisher;
    private String imageLink;

    public AddBookResponseDTO(UserBook userBook) {
        this.userBookId = userBook.getId();
        this.status = userBook.getStatus();
        this.ownerId = userBook.getOwner().getId();
        this.bookId = userBook.getBook().getId();
        this.title = userBook.getBook().getTitle();
        this.author = userBook.getBook().getAuthor();
        this.publisher = userBook.getBook().getPublisher();
        this.imageLink = userBook.getBook().getImageLink();
    }
}