package tns.com.ssir.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tns.com.ssir.dto.AuthResponse;
import tns.com.ssir.dto.LoginRequest;
import tns.com.ssir.dto.TokenRefreshRequest;
import tns.com.ssir.dto.TokenRefreshResponse;
import tns.com.ssir.security.JwtTokenProvider;
import tns.com.ssir.security.UserPrincipal;
import tns.com.ssir.security.CustomUserDetailsService;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService; // Inject custom User Details service

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // A. Generate both tokens statelessly using S3-compliant signing keys
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken) // Stateless JWT sent directly to client
                .username(userPrincipal.getUsername())
                .roles(roles)
                .companyId(userPrincipal.getCompanyId())
                .build();

        return ResponseEntity.ok(authResponse);
    }

    // --- STATELESS JWT TOKEN REFRESH ENDPOINT ---
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        // 1. Verify the cryptographic signature and scope of the incoming Refresh Token
        if (tokenProvider.validateRefreshToken(requestRefreshToken)) {
            // 2. Extract the user ID from the verified token payload
            Long userId = tokenProvider.getUserIdFromJWT(requestRefreshToken);

            // 3. Load user details from our service context
            UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserById(userId);
            
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal, null, userPrincipal.getAuthorities()
            );

            // 4. Generate a fresh Access Token and a rotated Refresh Token statelessly
            String newAccessToken = tokenProvider.generateAccessToken(authentication);
            String newRefreshToken = tokenProvider.generateRefreshToken(authentication); // Token Rotation pattern

            return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, newRefreshToken));
        } else {
            // Reject if the signature is invalid or expired
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token. Please log in again.");
        }
    }

    @GetMapping("/test-hash")
    public ResponseEntity<String> getTestHash() {
        String rawPassword = "Password123!";
        String encodedPassword = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(rawPassword);
        return ResponseEntity.ok(encodedPassword);
    }
}