package ae.gov.tdra.ssir.api.service;

import ae.gov.tdra.ssir.api.dto.RegistrationRequestDto;
import ae.gov.tdra.ssir.core.entity.RegistrationRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface RegistrationService {
    
    // Updated: Accepts file alongside DTO
    //RegistrationRequest submitRegistrationWithFile(RegistrationRequestDto dto, MultipartFile file);
	// Updated: Accepts a dynamic map of multiple files
    RegistrationRequest submitRegistrationWithFiles(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap);
    
    List<RegistrationRequest> getAllRegistrations();
    
    RegistrationRequest updateRegistrationStatus(Long id, String status, String comments);
    
 // Fetches a single registration with all documents pre-signed
    RegistrationRequest getRegistrationWithPresignedUrls(Long id);
}