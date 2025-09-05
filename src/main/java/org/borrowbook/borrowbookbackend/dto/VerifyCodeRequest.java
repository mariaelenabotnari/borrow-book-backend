package org.borrowbook.borrowbookbackend.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyCodeRequest {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Schema(example = "string@example.com")
    private String email;

    @Size()
    @NotBlank(message = "Code is required")
    private String code;
}

