package org.borrowbook.borrowbookbackend.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class GoogleBookDTO {
    private String id;
    private VolumeInfo volumeInfo;

    @Data
    public static class VolumeInfo {
        private String title;
        private List<String> authors;
        private String publisher;
        private String publishedDate;
        private String description;
        private List<IndustryIdentifier> industryIdentifiers;
        private ReadingModes readingModes;
        private int pageCount;
        private String printType;
        private List<String> categories;
        private double averageRating;
        private int ratingsCount;
        private String maturityRating;
        private boolean allowAnonLogging;
        private String contentVersion;
        private PanelizationSummary panelizationSummary;
        private ImageLinks imageLinks;
        private String language;
        private String previewLink;
        private String infoLink;
        private String canonicalVolumeLink;
    }

    @Data
    public static class IndustryIdentifier {
        private String type;
        private String identifier;
    }

    @Data
    public static class ReadingModes {
        private boolean text;
        private boolean image;
    }

    @Data
    public static class PanelizationSummary {
        private boolean containsEpubBubbles;
        private boolean containsImageBubbles;
    }

    @Data
    public static class ImageLinks {
        private String smallThumbnail;
        private String thumbnail;
    }
}
