package org.borrowbook.borrowbookbackend.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyCodeRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 5, message = "Username must be of at least 5 characters")
    private String username;

    @Size()
    @NotBlank(message = "Code is required")
    private String code;
}

