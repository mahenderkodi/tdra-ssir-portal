package ae.gov.tdra.ssir.api.controller;

import ae.gov.tdra.ssir.api.dto.RegistrationRequestDto;
import ae.gov.tdra.ssir.api.service.RegistrationService;
import ae.gov.tdra.ssir.core.entity.RegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    // 1. Submit Registration API
    @PostMapping
    public ResponseEntity<RegistrationRequest> submitRegistration(@Valid @RequestBody RegistrationRequestDto dto) {
        RegistrationRequest savedRequest = registrationService.submitRegistration(dto);
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