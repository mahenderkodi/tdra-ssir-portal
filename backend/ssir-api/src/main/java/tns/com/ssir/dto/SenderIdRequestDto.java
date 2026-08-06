package tns.com.ssir.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SenderIdRequestDto {
    @NotBlank(message = "Sender ID name is required")
    @Size(min = 2, max = 11, message = "Sender ID must be between 2 and 11 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Sender ID must contain only alphanumeric characters, dots, hyphens, or underscores")
    private String senderIdName;
}