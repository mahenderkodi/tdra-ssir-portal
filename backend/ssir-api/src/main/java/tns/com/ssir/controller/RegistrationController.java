package tns.com.ssir.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import tns.com.ssir.core.entity.RegistrationRequest;
import tns.com.ssir.dto.RegistrationRequestDto;
import tns.com.ssir.dto.RegistrationStatusUpdateResponse;
import tns.com.ssir.dto.RegistrationSuccessResponse;
import tns.com.ssir.dto.TrackingRequest;
import tns.com.ssir.dto.TrackingResponse;
import tns.com.ssir.security.UserPrincipal;
import tns.com.ssir.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private Validator validator;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegistrationSuccessResponse> submitRegistration(@RequestPart("registrationData") String registrationDataJson,MultipartHttpServletRequest request) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        RegistrationRequestDto dto = objectMapper.readValue(registrationDataJson, RegistrationRequestDto.class);

        Set<ConstraintViolation<RegistrationRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations); 
        }
        
        RegistrationRequest savedRequest = registrationService.submitRegistrationWithFiles(dto, request.getMultiFileMap());

        // Extract the generated plain-text temp password we temporarily stored in the entity
        String tempPassword = savedRequest.getRejectionReason();
        savedRequest.setRejectionReason(null); // Clean up entity field

        // --- NEW: DYNAMICALLY RESOLVE THE CORRECT USERNAME ---
        // Reads the representative's official email if present; otherwise, falls back to company email [1]
        String generatedUsername = (savedRequest.getCompany().getContacts() != null && !savedRequest.getCompany().getContacts().isEmpty())
                ? savedRequest.getCompany().getContacts().get(0).getOfficialEmail()
                : savedRequest.getCompany().getEmail();
        // -----------------------------------------------------

        RegistrationSuccessResponse successResponse = RegistrationSuccessResponse.builder()
                .trackingId(savedRequest.getTrackingId())
                .status(savedRequest.getCurrentStatus())
                .message("Your onboarding application has been successfully submitted.")
                .username(generatedUsername) // Updated to use the correctly resolved username [1]
                .tempPassword(tempPassword)
                .submittedAt(savedRequest.getCreatedAt())
                .build();

        return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
    }
    

	@GetMapping
	public ResponseEntity<List<RegistrationRequest>> getAllRegistrations() {
	     List<RegistrationRequest> list = registrationService.getAllRegistrations();
	     return ResponseEntity.ok(list);
	   }
    
    @GetMapping("/{id}")
    public ResponseEntity<RegistrationRequest> getRegistrationById(@PathVariable("id") Long id) {
        RegistrationRequest request = registrationService.getRegistrationWithPresignedUrls(id);
        return ResponseEntity.ok(request);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<RegistrationStatusUpdateResponse> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "comments", required = false) String comments) {
        
        RegistrationStatusUpdateResponse response = registrationService.updateRegistrationStatus(id, status, comments);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my-status")
    public ResponseEntity<TrackingResponse> getMyStatus(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserPrincipal principal) {
        
        if (principal.getCompanyId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No company associated with this user account.");
        }

        RegistrationRequest request = registrationService.getRegistrationByCompanyId(principal.getCompanyId());
        TrackingResponse trackingResponse = TrackingResponse.builder()
        		.trackingId(request.getTrackingId())
                .companyName(request.getCompany().getCompanyName())
                .currentStatus(request.getCurrentStatus())
                .submittedAt(request.getCreatedAt())
                .build();
        return ResponseEntity.ok(trackingResponse);
    }
    
    
    
 // 5. Secure Public Tracking API [3]
    @PostMapping("/track")
    public ResponseEntity<TrackingResponse> trackApplication(@Valid @RequestBody TrackingRequest trackingRequest) {
        RegistrationRequest request = registrationService.trackApplication(
                trackingRequest.getTrackingId()
                
        );
        
        TrackingResponse trackingResponse = TrackingResponse.builder()
        		.trackingId(request.getTrackingId())
                .companyName(request.getCompany().getCompanyName())
                .currentStatus(request.getCurrentStatus())
                .submittedAt(request.getCreatedAt())
                .build();
        return ResponseEntity.ok(trackingResponse);
    }
}