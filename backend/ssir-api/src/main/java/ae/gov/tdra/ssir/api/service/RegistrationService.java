package ae.gov.tdra.ssir.api.service;

import ae.gov.tdra.ssir.api.dto.RegistrationRequestDto;
import ae.gov.tdra.ssir.core.entity.RegistrationRequest;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface RegistrationService {
    
    // Updated: Accepts file alongside DTO
    RegistrationRequest submitRegistrationWithFile(RegistrationRequestDto dto, MultipartFile file);
    
    List<RegistrationRequest> getAllRegistrations();
    
    RegistrationRequest updateRegistrationStatus(Long id, String status, String comments);
}