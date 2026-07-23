package ae.gov.tdra.ssir.api.controller;

import ae.gov.tdra.ssir.api.dto.RegistrationRequestDto;
import ae.gov.tdra.ssir.api.service.RegistrationService;
import ae.gov.tdra.ssir.core.entity.RegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    // 1. Submit Registration API
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegistrationRequest> submitRegistration(
            @RequestPart("registrationData") String registrationDataJson,
            MultipartHttpServletRequest request) throws Exception { // Accepts dynamic multi-file map

        ObjectMapper objectMapper = new ObjectMapper();
        RegistrationRequestDto dto = objectMapper.readValue(registrationDataJson, RegistrationRequestDto.class);

        // Pass the dynamic multi-file map to the service layer
        RegistrationRequest savedRequest = registrationService.submitRegistrationWithFiles(dto, request.getMultiFileMap());
        return new ResponseEntity<>(savedRequest, HttpStatus.CREATED);
    }

    // 2. Get All Registrations API (Admin view)
    @GetMapping
    public ResponseEntity<List<RegistrationRequest>> getAllRegistrations() {
        List<RegistrationRequest> list = registrationService.getAllRegistrations();
        return ResponseEntity.ok(list);
    }

    // 3. Update Status API (Admin approval/rejection)
    @PutMapping("/{id}/status")
    public ResponseEntity<RegistrationRequest> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String comments) {
        
        RegistrationRequest updated = registrationService.updateRegistrationStatus(id, status, comments);
        return ResponseEntity.ok(updated);
    }
}