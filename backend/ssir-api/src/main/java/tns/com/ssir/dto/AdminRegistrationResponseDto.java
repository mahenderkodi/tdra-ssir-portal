package tns.com.ssir.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRegistrationResponseDto {
    private Long id;
    private String trackingId;
    private String companyName;
    private String companyType;
    private String proposedSenderId; // Dynamically resolved matching trackingId [3]
    private String representativeName;
    private String representativeEmail;
    private String currentStatus;
    private LocalDateTime submittedAt;
    private int documentCount;
}