package org.borrowbook.borrowbookbackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.borrowbook.borrowbookbackend.model.entity.Book;
import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowedBookDTO {
    private String title;
    private List<String> authors;
    private String imageLink;
    private String ownerUsername;

    public BorrowedBookDTO(BorrowRequest borrowRequest){
        Book book = borrowRequest.getUserBook().getBook();
        this.title = book.getTitle();
        this.authors = book.getAuthor();
        this.imageLink = book.getImageLink();
        this.ownerUsername = borrowRequest.getUserBook().getOwner().getUsername();
    }
}
