package org.borrowbook.borrowbookbackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 5, message = "Username must be of at least 5 characters")
    private String username;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Schema(example = "string@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be of at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one letter, one uppercase letter, and one number`")
    @Schema(example = "string")
    private String password;
}
