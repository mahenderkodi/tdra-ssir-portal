package tns.com.ssir.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken; 
    @Builder.Default
    private String tokenType = "Bearer";
    private String username;
    private List<String> roles;
    private Long companyId;
    private boolean firstTimeLogin;
}