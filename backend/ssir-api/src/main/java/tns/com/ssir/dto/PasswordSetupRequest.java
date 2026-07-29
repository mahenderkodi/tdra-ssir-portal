package tns.com.ssir.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordSetupRequest {
    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters long")
    private String password;
}