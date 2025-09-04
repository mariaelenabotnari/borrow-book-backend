package org.borrowbook.borrowbookbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class AuthenticationRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 5)
    private String username;

    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one letter, one uppercase letter, and one number`")
    @NotBlank(message = "Password is required")
    @Schema(example = "string")
    private String password;
}
