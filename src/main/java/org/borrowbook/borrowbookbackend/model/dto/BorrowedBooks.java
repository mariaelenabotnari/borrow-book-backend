package org.borrowbook.borrowbookbackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowedBooks {
    private String title;
    private List<String> authors;
    private String imageLink;
    private String ownerUsername;
}
