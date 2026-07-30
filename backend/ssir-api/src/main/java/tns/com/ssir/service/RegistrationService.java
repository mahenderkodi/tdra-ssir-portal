package tns.com.ssir.service;

import tns.com.ssir.core.entity.RegistrationRequest;
import tns.com.ssir.dto.RegistrationRequestDto;

import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface RegistrationService {
    
    RegistrationRequest submitRegistrationWithFiles(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap);
    
    List<RegistrationRequest> getAllRegistrations();
    
    RegistrationRequest updateRegistrationStatus(Long id, String status, String comments);
    
    RegistrationRequest getRegistrationWithPresignedUrls(Long id);
    
    RegistrationRequest trackApplication(String trackingId, String tradeLicenseNumber);
}