package ae.gov.tdra.ssir.api.service;

import ae.gov.tdra.ssir.api.dto.RegistrationRequestDto;
import ae.gov.tdra.ssir.core.entity.RegistrationRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface RegistrationService {
    
    RegistrationRequest submitRegistrationWithFiles(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap);
    
    List<RegistrationRequest> getAllRegistrations();
    
    RegistrationRequest updateRegistrationStatus(Long id, String status, String comments);
    
    RegistrationRequest getRegistrationWithPresignedUrls(Long id);
}