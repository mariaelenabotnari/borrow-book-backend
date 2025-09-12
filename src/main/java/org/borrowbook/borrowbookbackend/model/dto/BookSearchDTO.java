package org.borrowbook.borrowbookbackend.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookSearchDTO {
    private String googleBookId;
    private String title;
    private List<String> author;
    private String publisher;
    private String imageLink;

    public BookSearchDTO(GoogleBookDTO googleBook){
        this.googleBookId = googleBook.getId();
        this.title = googleBook.getVolumeInfo().getTitle();
        this.author = googleBook.getVolumeInfo().getAuthors();
        this.publisher = googleBook.getVolumeInfo().getPublisher();
        if (googleBook.getVolumeInfo().getImageLinks() != null)
            this.imageLink = googleBook.getVolumeInfo().getImageLinks().getThumbnail();
        else
            this.imageLink = null;
    }
}
