package ae.gov.tdra.ssir.api.service;

import ae.gov.tdra.ssir.api.dto.RegistrationRequestDto;
import ae.gov.tdra.ssir.core.entity.RegistrationRequest;
import java.util.List;

public interface RegistrationService {
    
    // Submits and persists onboarding requests
    RegistrationRequest submitRegistration(RegistrationRequestDto dto);
    
    // Retrieves all submissions for TDRA Administrators
    List<RegistrationRequest> getAllRegistrations();
    
    // Executes status updates (Approve/Reject)
    RegistrationRequest updateRegistrationStatus(Long id, String status, String comments);
}