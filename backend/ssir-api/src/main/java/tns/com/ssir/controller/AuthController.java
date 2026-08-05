package tns.com.ssir.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Operation; // Import Operation
import io.swagger.v3.oas.annotations.tags.Tag;       // Import Tag
import tns.com.ssir.dto.*;
import tns.com.ssir.security.JwtTokenProvider;
import tns.com.ssir.security.UserPrincipal;
import tns.com.ssir.service.RegistrationService;
import tns.com.ssir.core.entity.User;
import tns.com.ssir.core.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Portal", description = "Endpoints for user logins, credential setups, and token refreshes") // Groups endpoints [1]
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

    @PostMapping("/login")
    @Operation(summary = "Authenticate User", description = "Verifies credentials against the database and returns short-lived access tokens and refresh tokens.") // Documents method [1]
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
        
        // Read the actual first_time_login column from the database [1]
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
                .firstTimeLogin(user.isFirstTimeLogin()) // Informs Angular if redirect is needed [1]
                .build();

        return ResponseEntity.ok(authResponse);
    }

    // --- SECURE AUTHENTICATED PASSWORD UPDATE ENDPOINT ---
    // Reads user identity dynamically from the Bearer token [1]
    @PostMapping("/setup-password")
    @Operation(summary = "Setup Permanent Password", description = "Securely overwrites temporary credentials with a permanent BCrypt-hashed password.") // Documents method [1]
    public ResponseEntity<?> setupPassword(
            @Valid @RequestBody PasswordSetupRequest passwordRequest, // Updated [1]
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserPrincipal principal) { 

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User session expired."));

        if (!user.isFirstTimeLogin()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password has already been initialized.");
        }

        // Successfully compiles: getPassword() is now fully defined [1]
        user.setPasswordHash(passwordEncoder.encode(passwordRequest.getPassword()));
        user.setFirstTimeLogin(false); 
        userRepository.save(user);

        return ResponseEntity.ok("{\"message\": \"Your permanent password has been set successfully.\"}");
    }
 // --- FORGOT PASSWORD ENDPOINT ---
 // Production Secure: Returns a generic message regardless of email existence [1]
 @PostMapping("/forgot-password")
 public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
     registrationService.processForgotPassword(request);
     return ResponseEntity.ok("{\"message\": \"If an account exists for this email, a password reset link has been sent.\"}");
 }

 // --- RESET PASSWORD ENDPOINT ---
 @PostMapping("/reset-password")
 public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
     registrationService.resetPassword(request);
     return ResponseEntity.ok("{\"message\": \"Your password has been successfully updated. You can now log in.\"}");
 }
}