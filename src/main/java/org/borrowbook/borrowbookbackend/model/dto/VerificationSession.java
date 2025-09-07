package org.borrowbook.borrowbookbackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationSession {
    private String email;
    private String username;
    private String code;
    private int attemptCount;
}