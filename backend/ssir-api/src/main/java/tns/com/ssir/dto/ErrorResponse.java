package tns.com.ssir.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private List<String> details; // Holds individual field-level violations
    private String path;
}