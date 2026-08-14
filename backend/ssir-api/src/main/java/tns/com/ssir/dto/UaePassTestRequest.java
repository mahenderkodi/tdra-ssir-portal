package tns.com.ssir.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UaePassTestRequest {
    @NotBlank(message = "Authorization code is required.")
    private String code;

    @NotNull(message = "Acquisition timestamp is required.")
    private LocalDateTime acquiredAt; // When you copied the code from your browser
}