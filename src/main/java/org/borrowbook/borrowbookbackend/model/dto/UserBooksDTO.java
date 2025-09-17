package org.borrowbook.borrowbookbackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    public UserBooksDTO(UserBooksDTO userBooksDTO) {
        this.title = userBooksDTO.getTitle();
        this.authors = userBooksDTO.getAuthors();
        this.imageLink = userBooksDTO.getImageLink();
        this.status = userBooksDTO.getStatus();
    }
}