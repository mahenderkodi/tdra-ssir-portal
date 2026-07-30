package tns.com.ssir.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MockEmailDetails {
    private String to;
    private String subject;
    private String body;
}