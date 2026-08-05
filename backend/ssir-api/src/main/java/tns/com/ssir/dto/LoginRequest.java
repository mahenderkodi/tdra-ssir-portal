package tns.com.ssir.dto;

import io.swagger.v3.oas.annotations.media.Schema; // Import Schema [1]
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request body payload for authenticating users") // Class description [1]
public class LoginRequest {

    @NotBlank(message = "Username or Email is required")
    @Schema(description = "Registered username or official email address", example = "tdra_admin") // Field description [1]
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Schema(description = "Account password", example = "Password123!") // Field description [1]
    private String password;
}