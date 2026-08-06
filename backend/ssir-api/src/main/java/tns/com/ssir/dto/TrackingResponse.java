package tns.com.ssir.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackingResponse {
    private String trackingId;
    private String companyName;
    private String currentStatus;
    private String proposedSenderId;
    private LocalDateTime submittedAt;
   // private String feedbackComments; 
    
}