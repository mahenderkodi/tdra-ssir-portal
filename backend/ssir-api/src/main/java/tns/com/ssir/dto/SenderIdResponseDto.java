package tns.com.ssir.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SenderIdResponseDto {
    private Long id;
    private String senderIdName;
    private String status;
    private LocalDate expirationDate;
    private LocalDateTime createdAt;
}