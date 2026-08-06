package tns.com.ssir.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Imported
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Operation; 
import io.swagger.v3.oas.annotations.tags.Tag;       
import tns.com.ssir.dto.*;
import tns.com.ssir.security.JwtTokenProvider;
import tns.com.ssir.security.UserPrincipal;
import tns.com.ssir.security.CustomUserDetailsService; // Imported
import tns.com.ssir.service.RegistrationService;
import tns.com.ssir.core.entity.User;
import tns.com.ssir.core.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Portal", description = "Endpoints for user logins, credential setups, and token refreshes")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService userDetailsService; // Injected CustomUserDetailsService [1]

    @PostMapping("/login")
    @Operation(summary = "Authenticate User", description = "Verifies credentials against the database and returns short-lived access tokens and refresh tokens.")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        User user = userRepository.findById(userPrincipal.getId()).get();

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(userPrincipal.getUsername())
                .roles(roles)
                .companyId(userPrincipal.getCompanyId())
                .firstTimeLogin(user.isFirstTimeLogin()) 
                .build();

        return ResponseEntity.ok(authResponse);
    }

    // --- NEW: STATELESS JWT REFRESH TOKEN ENDPOINT [1] ---
    @PostMapping("/refresh")
    @Operation(summary = "Refresh Access Token", description = "Validates a secure, stateless JWT Refresh Token and issues a fresh Access Token and a rotated Refresh Token.")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        // 1. Verify the cryptographic signature and scope of the incoming Refresh Token
        if (tokenProvider.validateRefreshToken(requestRefreshToken)) {
            // 2. Extract the user ID from the verified token payload
            Long userId = tokenProvider.getUserIdFromJWT(requestRefreshToken);

            // 3. Load user details from our custom service [1]
            UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserById(userId);
            
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal, null, userPrincipal.getAuthorities()
            );

            // 4. Generate a fresh Access Token and a rotated Refresh Token statelessly (Token Rotation) [1]
            String newAccessToken = tokenProvider.generateAccessToken(authentication);
            String newRefreshToken = tokenProvider.generateRefreshToken(authentication); 

            return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, newRefreshToken));
        } else {
            // Reject if the signature is invalid, expired, or scoped incorrectly
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token. Please log in again.");
        }
    }

    // --- SECURE AUTHENTICATED PASSWORD UPDATE ENDPOINT ---
    @PostMapping("/setup-password")
    @Operation(summary = "Setup Permanent Password", description = "Securely overwrites temporary credentials with a permanent BCrypt-hashed password.")
    public ResponseEntity<?> setupPassword(
            @Valid @RequestBody PasswordSetupRequest passwordRequest, 
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserPrincipal principal) { 

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User session expired."));

        if (!user.isFirstTimeLogin()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password has already been initialized.");
        }

        user.setPasswordHash(passwordEncoder.encode(passwordRequest.getPassword()));
        user.setFirstTimeLogin(false); 
        userRepository.save(user);

        return ResponseEntity.ok("{\"message\": \"Your permanent password has been set successfully.\"}");
    }

    // --- FORGOT PASSWORD ENDPOINT ---
    @PostMapping("/forgot-password")
    @Operation(summary = "Request Password Reset", description = "Generates a secure, short-lived reset token and prints the simulated email link to your console.")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        registrationService.processForgotPassword(request);
        return ResponseEntity.ok("{\"message\": \"If an account exists for this email, a password reset link has been sent.\"}");
    }

    // --- RESET PASSWORD ENDPOINT ---
    @PostMapping("/reset-password")
    @Operation(summary = "Execute Password Reset", description = "Validates the secure reset token, encrypts the new password, and updates credentials.")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        registrationService.resetPassword(request);
        return ResponseEntity.ok("{\"message\": \"Your password has been successfully updated. You can now log in.\"}");
    }

    // --- TEMPORARY DEBUGGING ENDPOINT ---
    @GetMapping("/test-hash")
    @Operation(summary = "Generate BCrypt Hash", description = "Utility helper that outputs the local BCrypt hash for 'Password123!' to assist seeding.")
    public ResponseEntity<String> getTestHash() {
        String rawPassword = "Password123!";
        String encodedPassword = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(rawPassword);
        return ResponseEntity.ok(encodedPassword);
    }
}