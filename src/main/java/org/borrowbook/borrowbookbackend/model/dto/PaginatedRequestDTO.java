package org.borrowbook.borrowbookbackend.model.dto;

import lombok.Data;

@Data
public class PaginatedRequestDTO {
    private int pageIndex = 1;
    private int pageSize = 10;
}