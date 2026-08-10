package tns.com.ssir.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderIdResponseDto {
    private Long id;
    private String senderIdName;
    private String trackingId;      // For list and detail views
    private String status;
    private LocalDateTime createdAt; // Maps to "Submitted On"
    private LocalDate expirationDate;
    private String remarks;         // Maps to "Remarks" / feedback
    private String justification;   // Justification entered during creation
    private String companyName;     // Company name detail
    private String authLetterUrl;   // Secure presigned S3/MinIO URL for the uploaded file [2]
}