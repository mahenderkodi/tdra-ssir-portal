package tns.com.ssir.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import tns.com.ssir.core.entity.RegistrationRequest;
import tns.com.ssir.dto.RegistrationRequestDto;
import tns.com.ssir.dto.RegistrationSuccessResponse;
import tns.com.ssir.dto.TrackingRequest;
import tns.com.ssir.dto.TrackingResponse;
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

        RegistrationSuccessResponse successResponse = RegistrationSuccessResponse.builder()
                .trackingId(savedRequest.getTrackingId())
                .status(savedRequest.getCurrentStatus())
                .message("Your onboarding application has been successfully submitted to TDRA.")
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
    public ResponseEntity<RegistrationRequest> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "comments", required = false) String comments) {
        
        RegistrationRequest updated = registrationService.updateRegistrationStatus(id, status, comments);
        return ResponseEntity.ok(updated);
    }
    
 // 5. Secure Public Tracking API [3]
    @PostMapping("/track")
    public ResponseEntity<TrackingResponse> trackApplication(@Valid @RequestBody TrackingRequest trackingRequest) {
        RegistrationRequest request = registrationService.trackApplication(
                trackingRequest.getTrackingId(),
                trackingRequest.getTradeLicenseNumber()
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