package tns.com.ssir.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistrationStatusUpdateResponse {
    private String trackingId;
    private String currentStatus;
    private String userId;    
    private String proposedSenderId;
    private boolean emailSent;
    private MockEmailDetails mockEmailDetails;
}