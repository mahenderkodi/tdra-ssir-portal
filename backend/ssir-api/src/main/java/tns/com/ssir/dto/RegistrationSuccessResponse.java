package tns.com.ssir.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistrationSuccessResponse {
    private String trackingId;
    private String status;
    private String message;
    private String username;     // The representative's email acting as username
    private String tempPassword; // Plain-text temporary password shown once on the success screen
    private String proposedSenderId;
    private LocalDateTime submittedAt;
}