package org.borrowbook.borrowbookbackend.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class GoogleBookResponeDTO {
    List<GoogleBookDTO> items;
}
