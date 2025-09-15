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
        if(googleBook.getVolumeInfo() != null ){
            if(googleBook.getVolumeInfo().getTitle() != null) this.title = googleBook.getVolumeInfo().getTitle();
            if(googleBook.getVolumeInfo().getAuthors() != null) this.author = googleBook.getVolumeInfo().getAuthors();
            if(googleBook.getVolumeInfo().getPublisher() != null) this.publisher = googleBook.getVolumeInfo().getPublisher();
            if (googleBook.getVolumeInfo().getImageLinks() != null) {
                this.imageLink = googleBook.getVolumeInfo().getImageLinks().getThumbnail();
            }
        }
    }
}