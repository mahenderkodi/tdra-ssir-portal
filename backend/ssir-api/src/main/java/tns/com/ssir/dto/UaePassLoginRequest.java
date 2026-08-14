package tns.com.ssir.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UaePassLoginRequest {
    @NotBlank(message = "Authorization code is required.")
    private String code;
}