package org.borrowbook.borrowbookbackend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyCodeRequest {
    @NotBlank(message = "sessionId is required")
    private String sessionId;

    @NotBlank(message = "Code is required")
    private String code;
}

