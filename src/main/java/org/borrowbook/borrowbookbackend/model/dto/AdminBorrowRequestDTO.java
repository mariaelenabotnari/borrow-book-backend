package org.borrowbook.borrowbookbackend.model.dto;

import lombok.Data;

@Data
public class AdminBorrowRequestDTO {
    private Integer id;
    private String bookTitle;
    private String borrower;
    private String lender;
    private String status;
    private String requestDate;
    private String responseDate;

    public AdminBorrowRequestDTO(
            Integer id,
            String bookTitle,
            String borrower,
            String lender,
            String status,
            String requestDate,
            String responseDate
    ) {
        this.id = id;
        this.bookTitle = bookTitle;
        this.borrower = borrower;
        this.lender = lender;
        this.status = status;
        this.requestDate = requestDate;
        this.responseDate = responseDate;
    }
}
