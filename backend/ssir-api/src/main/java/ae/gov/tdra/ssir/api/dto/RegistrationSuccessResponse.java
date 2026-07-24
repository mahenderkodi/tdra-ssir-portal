package ae.gov.tdra.ssir.api.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistrationSuccessResponse {
    private String trackingId;
    private String status;
    private String message;
    private LocalDateTime submittedAt;
}