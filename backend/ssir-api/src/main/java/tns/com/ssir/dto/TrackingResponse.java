package tns.com.ssir.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingResponse {
    private String trackingId;
    private String companyName;
    private String currentStatus;
    private LocalDateTime submittedAt;
    private String feedbackComments; // Resolved: Included the required comments field [3]
    private List<TrackedDocumentDto> documents;
}