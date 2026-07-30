package tns.com.ssir.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistrationStatusUpdateResponse {
    private String trackingId;
    private String currentStatus;
    private String companyId; // populated only on APPROVED
    private String userId;    // populated only on APPROVED
    private boolean emailSent;
    private MockEmailDetails mockEmailDetails;
}