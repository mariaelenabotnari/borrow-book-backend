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
    @Size(min = 5)
    private String username;

    @Size(min = 6)
    @NotBlank(message = "Code is required")
    private String code;
}

