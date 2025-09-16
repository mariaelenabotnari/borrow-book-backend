package org.borrowbook.borrowbookbackend.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class PaginatedResult<T> {
    private final int pageIndex;
    private final int pageSize;
    private final long totalCount;
    private final int totalPages;
    private final List<T> items;

    public PaginatedResult(int pageIndex, int pageSize, long totalCount, List<T> items) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
        this.items = items;
    }

    public boolean hasNextPage() {
        return pageIndex < totalPages;
    }

    public boolean hasPreviousPage() {
        return pageIndex > 1;
    }
}