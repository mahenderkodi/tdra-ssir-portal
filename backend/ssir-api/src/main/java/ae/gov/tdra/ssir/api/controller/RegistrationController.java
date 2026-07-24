package ae.gov.tdra.ssir.api.controller;

import ae.gov.tdra.ssir.api.dto.RegistrationRequestDto;
import ae.gov.tdra.ssir.api.dto.RegistrationSuccessResponse;
import ae.gov.tdra.ssir.api.service.RegistrationService;
import ae.gov.tdra.ssir.core.entity.RegistrationRequest;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

   
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegistrationSuccessResponse> submitRegistration(@RequestPart("registrationData") String registrationDataJson, MultipartHttpServletRequest request) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        RegistrationRequestDto dto = objectMapper.readValue(registrationDataJson, RegistrationRequestDto.class);

        RegistrationRequest savedRequest = registrationService.submitRegistrationWithFiles(dto, request.getMultiFileMap());
     // 2. Map directly to your clean success DTO
        RegistrationSuccessResponse successResponse = RegistrationSuccessResponse.builder()
                .trackingId(savedRequest.getTrackingId())
                .status(savedRequest.getCurrentStatus())
                .message("Your onboarding application has been successfully submitted to TDRA.")
                .submittedAt(savedRequest.getCreatedAt())
                .build();

        return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
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
}