package tns.com.ssir.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStatusUpdateResponse {
    private String trackingId;
    private String currentStatus;
    private String remarks; // Holds the admin feedback comments/remarks [3]
}