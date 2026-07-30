package tns.com.ssir.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TrackingRequest {

    @NotBlank(message = "Tracking ID is required")
    private String trackingId;

    @NotBlank(message = "Trade License Number is required")
    private String tradeLicenseNumber;
}