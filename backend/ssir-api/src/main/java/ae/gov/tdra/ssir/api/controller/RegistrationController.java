package ae.gov.tdra.ssir.api.controller;

import ae.gov.tdra.ssir.api.dto.RegistrationRequestDto;
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

    // 1. Submit Registration API
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegistrationRequest> submitRegistration(
            @RequestPart("registrationData") String registrationDataJson,
            MultipartHttpServletRequest request) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        RegistrationRequestDto dto = objectMapper.readValue(registrationDataJson, RegistrationRequestDto.class);

        RegistrationRequest savedRequest = registrationService.submitRegistrationWithFiles(dto, request.getMultiFileMap());
        return new ResponseEntity<>(savedRequest, HttpStatus.CREATED);
    }
    
    // 4. Secure Inspection Endpoint (Updated with explicit "id" name mapping)
    @GetMapping("/{id}")
    public ResponseEntity<RegistrationRequest> getRegistrationById(@PathVariable("id") Long id) { // Added "id"
        RegistrationRequest request = registrationService.getRegistrationWithPresignedUrls(id);
        return ResponseEntity.ok(request);
    }
    
    // 3. Update Status API (Updated with explicit parameter name mappings)
    @PutMapping("/{id}/status")
    public ResponseEntity<RegistrationRequest> updateStatus(
            @PathVariable("id") Long id, // Added "id"
            @RequestParam("status") String status, // Added "status"
            @RequestParam(value = "comments", required = false) String comments) { // Added "comments"
        
        RegistrationRequest updated = registrationService.updateRegistrationStatus(id, status, comments);
        return ResponseEntity.ok(updated);
    }
}