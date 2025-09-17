package org.borrowbook.borrowbookbackend.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class PaginatedResultDTO<T> {
    private final int pageIndex;
    private final int pageSize;
    private final long totalCount;
    private final int totalPages;
    private final boolean hasNextPage;
    private final boolean hasPreviousPage;
    private final List<T> items;

    public PaginatedResultDTO(int pageIndex, int pageSize, long totalCount, List<T> items) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
        this.hasNextPage = pageIndex < totalPages;
        this.hasPreviousPage = pageIndex > 1;
        this.items = items;
    }
}